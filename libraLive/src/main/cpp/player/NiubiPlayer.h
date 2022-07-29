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
    NiubiPlayer(const char *source, JavaCallHelper *helper);

    ~NiubiPlayer();

    void prepare();

    void init_prepare();

    void start();

    void init_start();

    void stop();

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
    JavaCallHelper *callHelper;

    int isPlaying{0};

    AudioChannel *audioChannel = NULL;
    VideoChannel *videoChannel = NULL;

    AVFormatContext *formatContext = NULL;
    RenderFrameCallback renderFrameCallBack{};
};


#endif //LIBRALIVE_NIUBIPLAYER_H
