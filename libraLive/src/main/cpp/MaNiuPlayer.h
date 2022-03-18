//
// Created by windy on 2021/12/22.
//

#ifndef LIBRALIVE_MANIUPLAYER_H
#define LIBRALIVE_MANIUPLAYER_H

#include "CommonInclude.h"
#include "JavaCallHelper.h"
#include "VideoChannel.h"
#include "AudioChannel.h"

class MaNiuPlayer {

public:
    MaNiuPlayer(const char *source, JavaCallHelper *helper);

    ~MaNiuPlayer();

    void prepare();

    void init_prepare();

    void start();

    void init_start();

    void stop();

    void setRenderFrameCallBack(RenderFrameCallback renderFrameCallback);

public:
    // 线程
    pthread_t pid_pre;
    pthread_t pid_play;
    pthread_t pid_stop;

    // 地址
    char *dataSource;

    // java 调用
    JavaCallHelper *callHelper;

    int isPlaying {0};

    AudioChannel *audioChannel = NULL;
    VideoChannel *videoChannel = NULL;

    AVFormatContext *formatContext = NULL;
    RenderFrameCallback renderFrameCallBack;
};


#endif //LIBRALIVE_MANIUPLAYER_H
