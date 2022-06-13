#include "CommonInclude.h"

JavaVM *javaVM = NULL;

extern int register_com_windy_libralive_LibraPlayer(JNIEnv *env);

/**
 *
 * @param vm
 * @param reserved
 * @return
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("JNI_OnLoad() ");
    log("TEST");

    JNIEnv *env = nullptr;
    jint ret = vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (ret != JNI_OK) {
        return -1;
    }

    // 断言 env 不等于nullptr
    assert(env != nullptr);

    // 动态注册函数 registerNatives ->registerNativeMethods ->env->RegisterNatives
    if (!register_com_windy_libralive_LibraPlayer(env)) {
        return -1;
    }

    int fret = av_jni_set_java_vm(vm, 0);
    if (fret != 0) {
        LOGE("set java vm for ffmpeg failed. %s", av_err2str(fret));
    }

    javaVM = vm;
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    LOGI("JNI_OnUnload() ");
}