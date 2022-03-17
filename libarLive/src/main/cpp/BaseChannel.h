//
// Created by windy on 2021/12/23.
//

#ifndef LIBRALIVE_BASECHANNEL_H
#define LIBRALIVE_BASECHANNEL_H

#include "CommonInclude.h"

/**
 * 基类
 */
class BaseChannel {
public:
    BaseChannel(int id, AVCodecContext *avCodecContext, AVRational time_base);

    virtual ~BaseChannel();

    virtual void start();

    virtual void stop();

    static void releaseAvFrame(AVFrame **frame);

    static void releaseAvPacket(AVPacket **packet);

public:
    int id;
    AVCodecContext *mAvCodecContext;
    AVRational time_base;

    // 编码数据包队列
    message_queue<AVPacket *> packets;
    // 解码数据包队列
    message_queue<AVFrame *> frames;

    bool isPlaying;
    double audioTime; // 每一帧，音频计算后的时间值
    double videoTime; // 每一帧，视频计算后的时间值
};


#endif //LIBRALIVE_BASECHANNEL_H
