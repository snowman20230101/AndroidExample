//
// Created by windy on 2021/12/23.
//

//#include <queue>
#include "VideoChannel.h"

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

    this->fps = fps;
    frames.setSyncHandle(dropAvFrame);
    packets.setSyncHandle(dropAvPacket);
}

VideoChannel::~VideoChannel() {

}

void VideoChannel::start() {
    LOGD("VideoChannel::start");
    isPlaying = 1;

    frames.gotoWork();
    packets.gotoWork();
    // 解码
    pthread_create(&pid_decode, 0, task_decode, this);
    // 播放
    pthread_create(&pid_render, 0, task_render, this);
}

void VideoChannel::stop() {
    LOGD("VideoChannel::stop");
    isPlaying = 0;

    frames.backWork();
    packets.backWork();

    pthread_join(pid_decode, 0);
    pthread_join(pid_render, 0);
}

void VideoChannel::decode() {
    LOGD("VideoChannel::decode() start isPlaying=%d", isPlaying);

    AVPacket *packet = 0; // AVPacket 专门存放 压缩数据（H264）
    while (isPlaying) {

        // 生产快  消费慢
        // 消费速度比生成速度慢（生成100，只消费10个，这样队列会爆）
        // 内存泄漏点2，解决方案：控制队列大小
        if (isPlaying && frames.size() > 100) {
            // 休眠 等待队列中的数据被消费
            av_usleep(10 * 1000); // 微妙
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
        releaseAvPacket(&packet);

        // 代表了一个图像 (将这个图像先输出来)
        AVFrame *frame = av_frame_alloc(); // AVFrame 拿到解码后的原始数据包 （原始数据包，内部还是空的）

        // 从解码器中读取 解码后的数据包 AVFrame // 第三步： AVFrame 就有了值了 原始数据==YUV，最终打好的饭菜（原始数据）
        ret = avcodec_receive_frame(mAvCodecContext, frame);

        // 字幕解码

        // 需要更多的数据才能够进行解码
        if (ret == AVERROR(EAGAIN)) {
            // 重来，重新取，没有取到关键帧，重试一遍
            releaseAvFrame(&frame); // 内存释放点
            continue;
        } else if (ret != 0) {
            // 释放规则：必须是后面不再使用了，才符合释放的要求
            releaseAvFrame(&frame); // 内存释放点
            break;
        }

        // TODO AVPacket H264压缩数据 ----》 AVFrame YUV 原始数据
        // 终于取到了，解码后的视频数据（原始数据）
        // 再开一个线程 来播放 (流畅度)
        frames.push(frame); // 加入队列，为什么没有使用，无法满足"释放规则"
    }

    // 必须考虑的
    // 出了循环，就要释放
    // 释放规则：必须是后面不再使用了，才符合释放的要求
    releaseAvPacket(&packet);
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

    // 为上面两个初始化，并且，设置尺寸基本信息
    // int av_image_alloc(uint8_t *pointers[4], int linesizes[4], int w, int h, enum AVPixelFormat pix_fmt, int align);

    // void av_image_copy(uint8_t *dst_data[4], int dst_linesizes[4],
    //                   const uint8_t *src_data[4], const int src_linesizes[4],
    //                   enum AVPixelFormat pix_fmt, int width, int height);
//    av_image_copy();


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
            continue;
        }

        // int sws_scale(struct SwsContext *c, const uint8_t *const srcSlice[], const int srcStride[],
        //                  int srcSliceY, int srcSliceH, uint8_t *const dst[], const int dstStride[]);
        // 一帧yuv 转换 一帧rgba
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

        // 等一等 音频
        // av_usleep(result_delay * 1000000);

        // 知识控制来 视频慢下来来而已，还没有同步

        // 拿到视频 frame timestamp estimated using various heuristics, in stream time base
        this->videoTime = frame->best_effort_timestamp * av_q2d(this->time_base);
//        LOGE("videoTime=%lld", frame->best_effort_timestamp);

        // 拿到音频 播放时间基 audioChannel.audioTime
        double_t audioTime = this->audioChannel->audioTime; // 音频那边的值，是根据它来计算的

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
            // packtes  frames
            // 同步丢包操作
            // releaseAVFrame(&frame); 这样是不可以的

            this->frames.sync();

            // 继续 循环
            continue;

        } else {
            // 百分百完美 音频 视频 同步了
        }

        // 渲染，显示在屏幕上了
        // 渲染的两种方式：
        // 渲染一帧图像（宽，高，数据）
        this->renderFrameCallback(
                dst_data[0],
                mAvCodecContext->width,
                mAvCodecContext->height,
                dstStride[0]
        );

        releaseAvFrame(&frame); // 渲染完了，frame没用了，释放掉
    }

    releaseAvFrame(&frame);

    isPlaying = 0;

    // todo 回收注意点：
    // 1.一旦看到 "alloc" / "malloc" 关键字 ，满足释放规则的条件下，最后一定要尝试 xx_free()
    // 2.如果看到 "xxxContext" 此上下文，通常情况下，是申请了内存，满足释放规则的条件下，最后一定要尝试 xx_free()
    // 3.最后记得，标记归零 ，例如：isPlaying = 0;

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
    VideoChannel *channel = static_cast<VideoChannel *>(obj);
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
        AVFrame *frame = q.front();
        BaseChannel::releaseAvFrame(&frame);
        q.pop();
    }
}

void dropAvPacket(std::queue<AVPacket *> &qq) {
    if (!qq.empty()) {
        AVPacket *packet = qq.front();
        // BaseChannel::releaseAVPacket(&packet); // 关键帧没有了
        if (packet->flags != AV_PKT_FLAG_KEY) { // AV_PKT_FLAG_KEY == 关键帧
            BaseChannel::releaseAvPacket(&packet);
        }
        qq.pop();
    }
}