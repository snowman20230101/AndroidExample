#include <jni.h>
#include <string>
#include <android/log.h>

#include "breakpad/src/client/linux/handler/minidump_descriptor.h"
#include "breakpad/src/client/linux/handler/exception_handler.h"

bool DumpCallback(const google_breakpad::MinidumpDescriptor &descriptor, void *context, bool succeeded) {
    __android_log_print(ANDROID_LOG_ERROR, "ndk_crash", "Dump path: %s", descriptor.path());

    // 如果回调返回true，Breakpad将把异常视为已完全处理，禁止任何其他处理程序收到异常通知。
    // 如果回调返回false，Breakpad会将异常视为未处理，并允许其他处理程序处理它。 return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_windy_breakpadexample_MainActivity_initBreakpad(JNIEnv *env, jobject thiz, jstring path) {
    const char *cPath = env->GetStringUTFChars(path, NULL);

    google_breakpad::MinidumpDescriptor descriptor(cPath);
    static google_breakpad::ExceptionHandler eh(descriptor, NULL, DumpCallback, NULL, true, -1);

    env->ReleaseStringUTFChars(path, cPath);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_windy_breakpadexample_MainActivity_testNativeCrash(JNIEnv *env, jobject thiz) {
    int *i = NULL;
    *i = 1;
}

extern "C" JNIEXPORT jstring
JNICALL Java_com_windy_breakpadexample_MainActivity_getStringFromJni(JNIEnv *env, jobject thiz) {
    std::string  str = "hello";
    return env->NewStringUTF(str.c_str());
}