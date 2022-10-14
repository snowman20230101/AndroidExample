//
// Created by windy on 2021/12/23.
//

//#include <queue>
#include "VideoChannel.h"

extern "C" {
#include <libavfilter/buffersrc.h>
#include <libavfilter/buffersink.h>
}

//const char *filter_descr = "drawtext=fontfile=arial.ttf:fontcolor=red:fontsize=50:text='Shuo.Wang'";
//const char *filter_descr = "lutyuv='u=128:v=128'"; // 黑白
//const char *filter_descr = "boxblur"; // 模糊
//const char *filter_descr = "hflip"; // 视频镜像
const char *filter_descr = "hue='h=60:s=-3'";
//const char *filter_descr = "crop=2/3*in_w:2/3*in_h"; // TODO 为啥为会崩溃
//const char *filter_descr = "drawbox=x=100:y=100:w=100:h=100:color=pink@0.5";
//const char *filter_descr = "scale=78:24,transpose=cclock";

/**
 * 解码，线程回调
 * @param obj
 * @return
 */
void *task_decode(void *obj);

/**
 * 绘制，线程回调
 * @param obj
 * @return
 */
void *task_render(void *obj);

/**
 * 丢已经解码的图片，队列回调
 * @param q
 */
void dropAvFrame(std::queue<AVFrame *> &q);

void dropAvPacket(std::queue<AVPacket *> &q);

/**
 * 视频编码
 * @param id
 * @param avCodecContext
 * @param time_base
 * @param fps
 */
VideoChannel::VideoChannel(int id, AVCodecContext *avCodecContext, AVRational time_base, int fps)
        : BaseChannel(id, avCodecContext, time_base) {
    this->id = id;
    this->mAvCodecContext = avCodecContext;
    this->time_base = time_base;

    scaleFilter = new ScaleFilter(avCodecContext, time_base);
    scaleFilter->initFilter(filter_descr);

    this->fps = fps;
    frames.setSyncHandle(dropAvFrame);
    packets.setSyncHandle(dropAvPacket);
}

VideoChannel::~VideoChannel() {
    LOGI("VideoChannel::~VideoChannel()");
    DELETE(scaleFilter)
}

void VideoChannel::start() {
    LOGD("VideoChannel::start");
    isPlaying = 1;

    frames.gotoWork();
    packets.gotoWork();
    // 解码
    pthread_create(&pid_decode, nullptr, task_decode, this);
    // 播放
    pthread_create(&pid_render, nullptr, task_render, this);
}

void VideoChannel::stop() {
    LOGD("VideoChannel::stop()");
    isPlaying = 0;

    frames.backWork();
    packets.backWork();

    pthread_join(pid_decode, 0);
    pthread_join(pid_render, 0);
}

void VideoChannel::decode() {
    LOGD("VideoChannel::decode() start isPlaying=%d", isPlaying);

    AVPacket *packet = 0; // AVPacket 专门存放 压缩数据（H264）
    AVFrame *frame = av_frame_alloc();
    while (isPlaying) {
        if (isPlaying && frames.size() > 100) {
            // 休眠 等待队列中的数据被消费
            av_usleep(10 * 1000); // 微妙
//            LOGD("VideoChannel::decode() frames 数据有点多，睡一会儿");
            continue;
        }

        // 取出一个数据包 压缩数据
        int ret = packets.pop(packet);

        // 如果停止播放，跳出循环, 出了循环，就要释放
        if (!isPlaying) {
            break;
        }

        // 取出失败
        if (!ret) {
            LOGE("VideoChannel::decode() packets 取出失败");
            continue;
        }

        // 第一步：AVPacket 从队列取出一个饭盒
        // 把包丢给解码器
        // 走到这里，就证明，取到了待解码的视频数据包
        ret = avcodec_send_packet(mAvCodecContext, packet);

        // 重试
        if (ret != 0) {
            break;
        }

        // 走到这里，就证明，packet，用完毕了，成功了（给“解码器上下文”发送Packet成功），那么就可以释放packet了
        releaseAvPacket(packet);

        // 代表了一个图像 (将这个图像先输出来)

        // 从解码器中读取 解码后的数据包 AVFrame // 第三步： AVFrame 就有了值了 原始数据==YUV，最终打好的饭菜（原始数据）
        ret = avcodec_receive_frame(mAvCodecContext, frame);

        // 需要更多的数据才能够进行解码
        if (ret == AVERROR(EAGAIN)) {
            // 重来，重新取，没有取到关键帧，重试一遍
//            releaseAvFrame(&frame); // 内存释放点
            av_frame_unref(frame);
            LOGE("VideoChannel::decode() 重来，重新取，没有取到关键帧，重试一遍, ret=%s", av_err2str(ret));
            continue;
        } else if (ret != 0) {
            // 释放规则：必须是后面不再使用了，才符合释放的要求
//            releaseAvFrame(&frame); // 内存释放点
            av_frame_unref(frame);
            break;
        }

//        LOGD("VideoChannel::decode() width=%d, height=%d linesize=%d",
//             frame->width,
//             frame->height,
//             frame->linesize[0]
//        );

        // 滤镜方式
        doFilter(frame);
//        releaseAvFrame(&frame);
        av_frame_unref(frame);

        // 无滤镜方式
//        frames.push(frame);
    }

    releaseAvFrame(frame);
    releaseAvPacket(packet);
}

void VideoChannel::doFilter(AVFrame *frame) {
    if (!scaleFilter) {
        return;
    }

    // 滤镜输入
    int ret = av_buffersrc_add_frame_flags(scaleFilter->bufferSrc_ctx, frame,
                                           AV_BUFFERSRC_FLAG_KEEP_REF
    );
    if (ret < 0) {
        LOGE("VideoChannel::decode() Error while feeding the filtergraph.");
    } else {
//        LOGD("VideoChannel::decode() 输入滤镜成果");
    }

    AVFrame *filter_frame = av_frame_alloc();

    // 滤镜输出
    ret = av_buffersink_get_frame(scaleFilter->bufferSink_ctx, filter_frame);
    if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
        LOGE("VideoChannel::decode() 滤镜输出出问题了");
    }
    if (ret < 0) {
        LOGE("VideoChannel::decode() Error while 滤镜输出");
    } else {
//        LOGD("VideoChannel::decode() 取出滤镜成果");
    }

    frames.push(filter_frame);
}

void VideoChannel::render() {
    LOGD("VideoChannel::render() isPlaying=%d", isPlaying);

    // Sws视频   Swr音频
    // 原始图形宽和高，可以通过解码器拿到
    // 1.TODO 把元素的 yuv  --->  rgba
    swsContext = sws_getContext(
            mAvCodecContext->width, mAvCodecContext->height,
            mAvCodecContext->pix_fmt, // 原始图形信息相关的 （输入）
            mAvCodecContext->width, mAvCodecContext->height,
            AV_PIX_FMT_RGBA, // 目标 尽量 要和 元素的保持一致（输出）
            SWS_BILINEAR, NULL, NULL, NULL);


    AVFrame *frame = 0; // 存放原始数据 yuv

    // 2.TODO 给dst_data rgba 这种 申请内存
    uint8_t *dst_data[4];
    int dstStride[4];

    av_image_alloc(
            dst_data,
            dstStride,
            mAvCodecContext->width,
            mAvCodecContext->height,
            AV_PIX_FMT_RGBA,
            1
    );

    // 3.TODO 原始数据 格式转换的函数 （从队列中，拿到（原始）数据，一帧一帧的转换（rgba），一帧一帧的渲染到屏幕上）
    while (isPlaying) {
        int ret = frames.pop(frame);

        // 如果停止播放，跳出循环, 出了循环，就要释放
        if (!isPlaying) {
            break;
        }

        if (!ret) {
            LOGE(" VideoChannel::render() 获取视频帧失败 frames size is %d", frames.size());
            continue;
        }

        // 格式的转换 (yuv --> rgba)   frame->out_buffers == yuv是原始数据      dst_data是rgba格式的数据
        // src_linesize: 表示每一行存放的 字节长度
        sws_scale(swsContext,
                  frame->data,
                  frame->linesize,
                  0,
                  mAvCodecContext->height,
                  dst_data,
                  dstStride
        );

        // 一帧yuv 转换 一帧rgba，sws_scale函数执行完成后， （rgba）dst_data, dst_linesize---》显示到屏幕上

        // TODO 控制视频播放速率
        // 在视频渲染之前，根据 FPS 来控制视频帧

        // 得到当前帧的额外延时时间 == repeat_pict (2*fps)
        double extra_delay = frame->repeat_pict; // extra_delay = repeat_pict / (2*fps)

        // 根据FPS得到延时时间
        double base_delay = 1.0 / this->fps;

        // 得到最终 当前帧 时间延时时间
        double result_delay = extra_delay + base_delay;

        // 拿到视频 frame timestamp estimated using various heuristics, in stream time base
        this->videoTime = frame->best_effort_timestamp *
                          av_q2d(this->time_base); // timestamp(秒) = pts * av_q2d(time_base)

        // 拿到音频 播放时间基 audioChannel.audioTime
        double_t audioTime = this->audioChannel->audioTime; // 音频那边的值，是根据它来计算的
        LOGE("best_effort_timestamp=%lld, extra_delay=%f, audioTime=%f, videoTime=%f, pts=%lld, pkt_dts=%lld",
             frame->best_effort_timestamp,
             extra_delay, audioTime, videoTime,
             frame->pts, frame->pkt_dts
        );

        // 计算 音频 和 视频的 差值
        double time_diff = videoTime - audioTime;
//        LOGE("time_diff=%f, videoTime=%f, audioTime=%f", time_diff, this->videoTime, audioTime);

        if (time_diff > 0) {
            // 说明：视频快一些，音频慢一些
            // 那就要，比音频播放的时间，久一点才行（等待音频），让音频自己追上来

            // 灵活等待【慢下来，睡眠的方式】
            if (time_diff > 1) {
                av_usleep((result_delay * 2) * 1000000); // 久一点
            } else {
                // 0 ~ 1 说明相差不大
                av_usleep((time_diff + result_delay) * 1000000);
            }

        } else if (time_diff < 0) {
            // 说明：视频慢一写，音频快一些
            // 那就要，比音频播放的时间，快一点才行（追音频），追赶音频
            // 丢帧，把冗余的帧丢弃，就快起来了
            this->frames.sync();
            // 继续 循环
            continue;

        } else {
            // 百分百完美 音频 视频 同步了
            LOGD("VideoChannel::render() 百分百完美 音频 视频 同步了");
        }

        // 渲染一帧图像（宽，高，数据）
        this->renderFrameCallback(
                dst_data[0],
                dstStride[0],
                mAvCodecContext->width,
                mAvCodecContext->height
        );

        releaseAvFrame(frame); // 渲染完了，frame没用了，释放掉
    }

    releaseAvFrame(frame);

    isPlaying = 0;
    av_freep(&dst_data[0]);

    sws_freeContext(swsContext);
    swsContext = NULL;

    LOGD("VideoChannel::render() function end isPlaying=%d", isPlaying);
}

void VideoChannel::setRenderFrameCallBack(RenderFrameCallback renderFrameCallback) {
    this->renderFrameCallback = renderFrameCallback;
}

void VideoChannel::setAudioChannel(AudioChannel *audioChannel) {
    this->audioChannel = audioChannel;
}

void *task_decode(void *obj) {
    VideoChannel *channel = static_cast<VideoChannel *>(obj);
    if (channel) {
        channel->decode();
    }

    return 0;
}

void *task_render(void *obj) {
    auto *channel = static_cast<VideoChannel *>(obj);
    if (channel) {
        channel->render();
    }

    return 0;
}

/**
 * 丢包
 * @param q
 */
void dropAvFrame(std::queue<AVFrame *> &q) {
    if (!q.empty()) {
        LOGD("VideoChannel:: dropAvFrame() AVFrame queue size %d", q.size());
        AVFrame *frame = q.front();
        BaseChannel::releaseAvFrame(frame);
        q.pop();
    }
}

void dropAvPacket(std::queue<AVPacket *> &qq) {
    if (!qq.empty()) {
        AVPacket *packet = qq.front();
        LOGD("VideoChannel:: dropAvPacket() AVPacket queue size %d flags is %d",
             qq.size(),
             packet->flags
        );
        // BaseChannel::releaseAVPacket(&packet); // 关键帧没有了
        if (packet->flags != AV_PKT_FLAG_KEY) { // AV_PKT_FLAG_KEY == 关键帧
            BaseChannel::releaseAvPacket(packet);
        }
        qq.pop();
    }
}