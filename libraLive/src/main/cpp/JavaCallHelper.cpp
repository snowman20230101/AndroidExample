//
// Created by windy on 2021/12/23.
//

#include "JavaCallHelper.h"

JavaCallHelper::JavaCallHelper(JavaVM *vm, JNIEnv *env, jobject instance) {
    LOGI("JavaCallHelper::JavaCallHelper() vm=%d", (vm != NULL));
    this->vm = vm;
    // 如果在主线程 回调
    this->env = env;
    // 一旦涉及到 jobject 跨方法 跨线程 就需要创建全局引用
    this->instance = env->NewGlobalRef(instance);

    jclass clazz = env->GetObjectClass(this->instance);
    onErrorId = env->GetMethodID(clazz, "onError", "(I)V");
    onPrepareId = env->GetMethodID(clazz, "onPrepare", "()V");

    jweak weakReference = env->NewWeakGlobalRef(instance);
    jboolean isSame = env->IsSameObject(this->instance, instance);

}

JavaCallHelper::~JavaCallHelper() {
    env->DeleteGlobalRef(instance);
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