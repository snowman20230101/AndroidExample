//
// Created by windy on 2021/12/23.
//

#ifndef LIBRALIVE_AUDIOCHANNEL_H
#define LIBRALIVE_AUDIOCHANNEL_H

#include "BaseChannel.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

#include "CommonInclude.h"
#include "AudioFilter.h"

class AudioChannel : public BaseChannel {
public:
    AudioChannel(int id, AVCodecContext *avCodecContext, AVRational time_base);

    ~AudioChannel() override;

    void start() override;

    void stop() override;

    void decode();

    void doFilter(AVFrame *frame);

    void init_start();

    int getPcm();

public:
    //  定义一个缓冲区
    uint8_t *out_buffers = 0;

    int out_channels = 0;
    int out_sample_size = 0;
    int out_sample_rate = 0;

private:
    // 线程ID
    pthread_t pid_audio_decode{};
    pthread_t pid_audio_play{};

    /**
     * OpenSL ES
     */

    // 引擎与引擎接口
    SLObjectItf engineObject = NULL;
    // 接口
    SLEngineItf engineInterface = NULL;
    // 混音器
    SLObjectItf outputMixObject = NULL;
    // 播放器
    SLObjectItf bqPlayerObject = NULL;
    // 播放器接口
    SLPlayItf bqPlayerInterface = NULL;
    // 队列结构
    SLAndroidSimpleBufferQueueItf bqPlayerBufferQueueInterface = NULL;

    // 音频转换 ==（重采样）
    SwrContext *swrContext = NULL;

    AudioFilter *audioFilter = NULL;
};


#endif //LIBRALIVE_AUDIOCHANNEL_H
