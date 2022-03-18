#include <string>
#include "CommonInclude.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_windy_libralive_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
    std::string version = av_version_info();
    std::string hello = "ffmpeg:" + version;

    jint envVersion = env->GetVersion();

    LOGD("JNI VERSION:%d", envVersion);

    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_windy_libralive_MainActivity_testThrow(JNIEnv *env, jobject thiz, jstring code) {

    const char *chrCode = env->GetStringUTFChars(code, JNI_FALSE);

    LOGE("参数是： %s", chrCode);

    // TODO 1.
    jclass clazz = env->GetObjectClass(thiz);

    // TODO 2.
    jmethodID testID = env->GetMethodID(clazz, "test", "()V");

    // TODO 3.
    env->CallVoidMethod(thiz, testID);

    // 确定是否抛出异常。在本地代码调用ExceptionClear()或Java代码处理异常之前，异常会一直抛出
//    jthrowable jthrow = env->ExceptionOccurred();
//    // ExceptionCheck()检查是否发生了异常，若有异常返回JNI_TRUE，否则返回JNI_FALSE
//    if (jthrow && env->ExceptionCheck()) {
//        env->ExceptionDescribe();//将堆栈的异常和回溯打印到系统错误报告通道，例如stderr。这是为调试提供的便利例程。
//        env->ExceptionClear();//清除当前正在抛出的任何异常。如果当前没有抛出异常，则此例程无效。
//        LOGI("Run Java method find exception.");
//    }
//
//    jclass arithmeticExceptionCls = env->FindClass("java/lang/ArithmeticException");
//    if (NULL != arithmeticExceptionCls) {
//        // 使用message指定的消息从指定的类构造一个异常对象，并导致抛出该异常
//        env->ThrowNew(arithmeticExceptionCls, "Throw New Exception: divide by zero 哈哈");
//    }

    env->ReleaseStringUTFChars(code, chrCode);

    LOGI("Run After JNI Throw New Exception.");
}