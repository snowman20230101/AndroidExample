//
// Created by windy on 2022/3/18.
//

#include "NiubiPlayer.h"
#include "CommonInclude.h"

#include <android/bitmap.h>
#include <android/native_window_jni.h>

extern JavaVM *javaVM;

ANativeWindow *window = NULL;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

NiubiPlayer *player = NULL;
JavaCallHelper *javaCallHelper;

/**
 * 绘制回调
 * @param src_data
 * @param line_size
 * @param w
 * @param ht
 */
void renderFrameCallBack(uint8_t *src_data, int line_size, int w, int ht);

static void com_windy_libralive_LibraPlayer_native_prepare(JNIEnv *env, jobject object,
                                                           jstring data_source) {
    const char *source = env->GetStringUTFChars(data_source, 0);
    javaCallHelper = new JavaCallHelper(javaVM, env, object);
    player = new NiubiPlayer(source, javaCallHelper);
    player->setRenderFrameCallBack(renderFrameCallBack);
    player->prepare();
    env->ReleaseStringUTFChars(data_source, source);
}

static void com_windy_libralive_LibraPlayer_native_start(JNIEnv *env, jobject object) {
    if (player) {
        player->start();
    }
}

static void com_windy_libralive_LibraPlayer_native_stop(JNIEnv *env, jobject object) {
    if (player) {
        player->stop();
    }

    DELETE(javaCallHelper);
}

static void com_windy_libralive_LibraPlayer_native_release(JNIEnv *env, jobject object) {
    pthread_mutex_lock(&mutex);
    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }

    pthread_mutex_unlock(&mutex);
}

static void com_windy_libralive_LibraPlayer_native_setSurface(JNIEnv *env, jobject object,
                                                              jobject surface) {
    pthread_mutex_lock(&mutex);

    if (window) {
        LOGD("native_setSurface() ANativeWindow_release()");
        ANativeWindow_release(window);
        window = nullptr;
    }

    window = ANativeWindow_fromSurface(env, surface);
    pthread_mutex_unlock(&mutex);
}

/**
 * 动态注册，jni函数
 * @param env
 * @param className
 * @param nativeMethod
 * @param methodNumber
 * @return
 */
static int registerNativesMethods(JNIEnv *env, const char *className,
                                  JNINativeMethod *nativeMethods, int methodNumber) {
    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, nativeMethods, methodNumber) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

int register_com_windy_libralive_LibraPlayer(JNIEnv *env) {
    const char *className = "com/windy/libralive/external/LibraLivePlayer";

    JNINativeMethod methods[] = {
            {"native_prepare",    "(Ljava/lang/String;)V",     (void *) com_windy_libralive_LibraPlayer_native_prepare},
            {"native_start",      "()V",                       (void *) com_windy_libralive_LibraPlayer_native_start},
            {"native_stop",       "()V",                       (void *) com_windy_libralive_LibraPlayer_native_stop},
            {"native_release",    "()V",                       (void *) com_windy_libralive_LibraPlayer_native_release},
            {"native_setSurface", "(Landroid/view/Surface;)V", (void *) com_windy_libralive_LibraPlayer_native_setSurface}
    };

    return registerNativesMethods(env, className, methods, (sizeof(methods) / sizeof(methods[0])));
}

void renderFrameCallBack(uint8_t *src_data, int width, int height, int src_liinesize) {
    pthread_mutex_lock(&mutex);

    if (!window) {
        pthread_mutex_unlock(&mutex);
        return;
    }

    // 设置窗口属性
    ANativeWindow_setBuffersGeometry(window, width, height, WINDOW_FORMAT_RGBA_8888);

    /**
     * int32_t width; // 水平显示的像素
     * int32_t height; // 垂直显示的像素
     * int32_t format; // 缓冲区格式 WINDOW_FORMAT_*
     * int32_t stride; // 缓冲区在内存中一行的像素，可能 >= width
     * void* bits; // 实际位数
     * uint32_t reserved[6]; // 不要碰
     */
    ANativeWindow_Buffer windowBuffer; // 缓冲区

    if (ANativeWindow_lock(window, &windowBuffer, 0)) {
        ANativeWindow_release(window);
        window = 0;
        pthread_mutex_unlock(&mutex);
        return;
    }

    // 填数据到buffer，其实就是修改数据
    uint8_t *dst_data = static_cast<uint8_t *>(windowBuffer.bits);
    int lineSize = windowBuffer.stride * 4; // RGBA 相当于是 每一个像素点 * rgba

    // 下面就是逐行Copy了
    for (int i = 0; i < windowBuffer.height; ++i) {
        // 一行 一行 的 Copy 到 Android屏幕上
        memcpy(dst_data + i * lineSize, src_data + i * src_liinesize, lineSize);
    }

    ANativeWindow_unlockAndPost(window);
    pthread_mutex_unlock(&mutex);
}