#include <string>
#include "CommonInclude.h"

extern "C" {
#include <librtmp/rtmp.h>
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_windy_libralive_external_NativeCaseTesting_00024Companion_stringFromJNI(JNIEnv *env,
                                                                                 jobject /* this */) {
    std::string version = av_version_info();
    std::string hello = "ffmpeg:" + version;

    jint envVersion = env->GetVersion();
    int rtmpVersion = RTMP_LibVersion();
    hello.append("\n rtmp:" + std::to_string(rtmpVersion));

    LOGD("JNI VERSION:%d, rtmpVersion=%d", envVersion, rtmpVersion);

//    cocos2d::CCLOG("", "");


    /**
     * 打印版本
     */
    char strBuffer[1024 * 4];
    strcat(strBuffer, "libavcodec : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVCODEC_VERSION));
    strcat(strBuffer, "\nlibavformat : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVFORMAT_VERSION));
    strcat(strBuffer, "\nlibavutil : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVUTIL_VERSION));
    strcat(strBuffer, "\nlibavfilter : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVFILTER_VERSION));
    strcat(strBuffer, "\nlibswresample : ");
    strcat(strBuffer, AV_STRINGIFY(LIBSWRESAMPLE_VERSION));
    strcat(strBuffer, "\nlibswscale : ");
    strcat(strBuffer, AV_STRINGIFY(LIBSWSCALE_VERSION));
    strcat(strBuffer, "\navcodec_configure : \n");
    strcat(strBuffer, avcodec_configuration());
    strcat(strBuffer, "\navcodec_license : ");
    strcat(strBuffer, avcodec_license());
    LOGE("GetFFmpegVersion\n%s", strBuffer);

    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_windy_libralive_external_NativeCaseTesting_00024Companion_testThrow(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jstring code) {

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


extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_windy_libralive_external_NativeCaseTesting_00024Companion_testStackOverFlow(JNIEnv *env,
                                                                                     jobject obj,
                                                                                     jint count,
                                                                                     jstring config) {
    jobjectArray str_array = NULL;
    jclass cls_string = NULL;
    jmethodID mid_string_init;
    jobject obj_str = NULL;
    const char *c_str_sample = NULL;
    char buff[256];
    int i;

    // 保证至少可以创建3个局部引用（str_array，cls_string，obj_str）
//    if (env->EnsureLocalCapacity(3) != JNI_OK) {
//        return NULL;
//    }

    c_str_sample = (env)->GetStringUTFChars(config, NULL);
    if (c_str_sample == NULL) {
        return NULL;
    }

    cls_string = (env)->FindClass("java/lang/String");
    if (cls_string == NULL) {
        return NULL;
    }

    // 获取String的构造方法
    mid_string_init = (env)->GetMethodID(cls_string, "<init>", "()V");
    if (mid_string_init == NULL) {
        (env)->DeleteLocalRef(cls_string);
        return NULL;
    }
    obj_str = (env)->NewObject(cls_string, mid_string_init);
    if (obj_str == NULL) {
        (env)->DeleteLocalRef(cls_string);
        return NULL;
    }

    // 创建一个字符串数组
    str_array = (env)->NewObjectArray(count, cls_string, obj_str);
    if (str_array == NULL) {
        (env)->DeleteLocalRef(cls_string);
        (env)->DeleteLocalRef(obj_str);
        return NULL;
    }

    // 给数组中每个元素赋值
    for (i = 0; i < count; ++i) {
        memset(buff, 0, sizeof(buff));   // 初始一下缓冲区
        sprintf(buff, c_str_sample, i);
        jstring newStr = (env)->NewStringUTF(buff);
        (env)->SetObjectArrayElement(str_array, i, newStr);
    }

    // 释放模板字符串所占的内存
    (env)->ReleaseStringUTFChars(config, c_str_sample);

    // 释放局部引用所占用的资源
    (env)->DeleteLocalRef(cls_string);
    (env)->DeleteLocalRef(obj_str);

    return str_array;
}