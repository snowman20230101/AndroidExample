#include <jni.h>

#include <string>
#include "CommonInclude.h"
#include "MaNiuPlayer.h"

#include <android/bitmap.h>
#include <android/native_window_jni.h>

#include "MaNiuPlayer.h"

JavaVM *javaVM = NULL;
ANativeWindow *window = NULL;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

MaNiuPlayer *player = NULL;
JavaCallHelper *javaCallHelper;

/**
 * 绘制回调
 * @param data
 * @param lineszie
 * @param w
 * @param ht
 */
void renderFrameCallBack(uint8_t *data, int lineszie, int w, int ht);

/**
 *
 * @param vm
 * @param reserved
 * @return
 */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("JNI_OnLoad() ");

    int ret = av_jni_set_java_vm(vm, 0);

    if (ret != 0) {
        LOGE("set java vm failed. %s", av_err2str(ret));
    }

    javaVM = vm;
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_windy_libarlive_LibarLivePlayer_native_1prepare(JNIEnv *env, jobject thiz,
                                                         jstring data_source) {
    // TODO: implement native_prepare()
    const char *source = env->GetStringUTFChars(data_source, 0);
    javaCallHelper = new JavaCallHelper(javaVM, env, thiz);
    player = new MaNiuPlayer(source, javaCallHelper);
    player->setRenderFrameCallBack(renderFrameCallBack);
    player->prepare();
    env->ReleaseStringUTFChars(data_source, source);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_windy_libarlive_LibarLivePlayer_native_1start(JNIEnv *env, jobject thiz) {
    // TODO: implement native_start()

    if (player) {
        player->start();
    }

}
extern "C"
JNIEXPORT void JNICALL
Java_com_windy_libarlive_LibarLivePlayer_native_1stop(JNIEnv *env, jobject thiz) {
    // TODO: implement native_stop()
    if (player) {
        player->stop();
    }

    DELETE(javaCallHelper);
//    DELETE(player);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_windy_libarlive_LibarLivePlayer_native_1release(JNIEnv *env, jobject thiz) {
    // TODO: implement native_release()
    pthread_mutex_lock(&mutex);
    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }

    pthread_mutex_unlock(&mutex);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_windy_libarlive_LibarLivePlayer_native_1setSurface(JNIEnv *env, jobject thiz,
                                                            jobject surface) {
    // TODO: implement native_setSurface()
    pthread_mutex_lock(&mutex);

    if (window) {
        LOGD("native_setSurface() ANativeWindow_release()");
        ANativeWindow_release(window);
        window = NULL;
    }

    window = ANativeWindow_fromSurface(env, surface);
    pthread_mutex_unlock(&mutex);
}

void renderFrameCallBack(uint8_t *src_data, int width, int height, int src_liinesize) {
    pthread_mutex_lock(&mutex);

    if (!window) {
        pthread_mutex_unlock(&mutex);
        return;
    }

    // 设置窗口属性
    ANativeWindow_setBuffersGeometry(window, width, height, WINDOW_FORMAT_RGBA_8888);

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