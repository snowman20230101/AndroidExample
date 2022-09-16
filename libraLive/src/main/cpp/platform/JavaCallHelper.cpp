//
// Created by windy on 2021/12/23.
//

#include "JavaCallHelper.h"

JavaCallHelper::JavaCallHelper(JavaVM *vm, JNIEnv *env, jobject instance) {
    LOGD("JavaCallHelper::JavaCallHelper() vm=%d", (vm != NULL));
    this->vm = vm;
    // 如果在主线程 回调
    this->env = env;
    // 一旦涉及到 jobject 跨方法 跨线程 就需要创建全局引用
    this->instance = env->NewGlobalRef(instance);

    this->clazz = env->GetObjectClass(this->instance);
    onErrorId = env->GetMethodID(clazz, "onError", "(I)V");
    onPrepareId = env->GetMethodID(clazz, "onPrepare", "()V");
}

JavaCallHelper::~JavaCallHelper() {
    LOGI("JavaCallHelper::~JavaCallHelper()");
    env->DeleteGlobalRef(instance);
    env->DeleteLocalRef(clazz);
}

void JavaCallHelper::onError(int thread, int error) {
    // 主线程
    if (thread == THREAD_MAIN) {
        env->CallVoidMethod(instance, onErrorId, error);
    } else {
        // 子线程
        JNIEnv *env = nullptr;
        // 获得属于我这一个线程的jnienv
        vm->AttachCurrentThread(&env, 0);
        env->CallVoidMethod(instance, onErrorId, error);
        vm->DetachCurrentThread();
    }
}

void JavaCallHelper::onPrepare(int thread) {
    if (thread == THREAD_MAIN) {
        env->CallVoidMethod(instance, onPrepareId);
    } else {
        //  子线程
        JNIEnv *env = nullptr;
        // 获得属于我这一个线程的jnienv
        vm->AttachCurrentThread(&env, 0);
        env->CallVoidMethod(instance, onPrepareId);
        vm->DetachCurrentThread();
    }
}