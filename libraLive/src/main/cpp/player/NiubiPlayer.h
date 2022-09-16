//
// Created by windy on 2021/12/22.
//

#ifndef LIBRALIVE_NIUBIPLAYER_H
#define LIBRALIVE_NIUBIPLAYER_H

#include "CommonInclude.h"
#include "JavaCallHelper.h"
#include "VideoChannel.h"
#include "AudioChannel.h"

class NiubiPlayer {

public:
    NiubiPlayer(const char *source, JavaVM *vm, JNIEnv *env, jobject instance);

    ~NiubiPlayer();

    void prepare();

    // 线程
    void init_prepare();

    void start();

    // 线程
    void init_start();

    void stop();

    // 线程
    void init_stop();

    void setRenderFrameCallBack(RenderFrameCallback renderFrameCallback);

    /**
     * 释放封装格式上下文
     */
    void releaseFormatContext();

public:
    // 线程
    pthread_t pid_pre{};
    pthread_t pid_play{};
    pthread_t pid_stop{};

    // 地址
    char *dataSource;

    // java 调用
    JavaCallHelper *callHelper = NULL;

    int isPlaying{0};

    AudioChannel *audioChannel = NULL;
    VideoChannel *videoChannel = NULL;

    AVFormatContext *formatContext = NULL;
    RenderFrameCallback renderFrameCallBack{};

    int64_t duration = 0;
};


#endif //LIBRALIVE_NIUBIPLAYER_H
