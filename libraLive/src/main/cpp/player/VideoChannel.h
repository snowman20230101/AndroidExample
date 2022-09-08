//
// Created by windy on 2021/12/23.
//

#ifndef LIBRALIVE_VIDEOCHANNEL_H
#define LIBRALIVE_VIDEOCHANNEL_H


#include "BaseChannel.h"
#include "AudioChannel.h"
#include "ScaleFilter.h"

/**
 * 1、解码
 * 2、播放
 */
typedef void (*RenderFrameCallback)(uint8_t *, int, int, int);

class VideoChannel : public BaseChannel {
public:
    VideoChannel(int id, AVCodecContext *avCodecContext, AVRational time_base, int fps);

    ~VideoChannel();

    void start() override;

    void stop() override;

    void decode();

    void render();

    void setRenderFrameCallBack(RenderFrameCallback renderFrameCallback);

    void setAudioChannel(AudioChannel *audioChannel);

private:
    pthread_t pid_decode {};
    pthread_t pid_render {};

    int fps = 0;
    RenderFrameCallback renderFrameCallback {};
    //
    SwsContext *swsContext = NULL;
    AudioChannel *audioChannel = NULL;

    ScaleFilter *scaleFilter = NULL;
};


#endif //LIBRALIVE_VIDEOCHANNEL_H
