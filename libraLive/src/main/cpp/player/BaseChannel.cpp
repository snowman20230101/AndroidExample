//
// Created by windy on 2021/12/23.
//

#include "BaseChannel.h"

BaseChannel::BaseChannel(int id, AVCodecContext *avCodecContext, AVRational time_base) {
    LOGD("BaseChannel::BaseChannel() streamId=%d", id);
    this->id = id;
    this->mAvCodecContext = avCodecContext;
    this->time_base = time_base;
    frames.setReleaseCallBack(releaseAvFrame);
    packets.setReleaseCallBack(releaseAvPacket);
}

BaseChannel::~BaseChannel() {
    this->frames.clear();
    this->packets.clear();
    if (mAvCodecContext) {
        avcodec_close(mAvCodecContext);
        avcodec_free_context(&mAvCodecContext);
        mAvCodecContext = NULL;
    }

    LOGI("BaseChannel::~BaseChannel() frames size is %d, packets size is %d",
         frames.size(),
         packets.size()
    );
}

void BaseChannel::releaseAvFrame(AVFrame *&frame) {
    if (frame) {
        av_frame_free(&frame);
        frame = 0;
    }
}

void BaseChannel::releaseAvPacket(AVPacket *&packet) {
    if (packet) {
        av_packet_free(&packet);
        packet = 0;
    }
}

void BaseChannel::start() {

}

void BaseChannel::stop() {

}