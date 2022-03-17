#include <string>
#include "CommonInclude.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_windy_libarlive_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
    std::string version = av_version_info();
    std::string hello = "ffmpeg:" + version;
    return env->NewStringUTF(hello.c_str());
}