//
// Created by windy on 2021/12/22.
//


#include "MaNiuPlayer.h"

void *task_init_prepare(void *obj);

void *task_init_start(void *obj);

void *task_init_stop(void *obj);

MaNiuPlayer::MaNiuPlayer(const char *source, JavaCallHelper *helper) {
    this->callHelper = helper;
    this->dataSource = new char[strlen(source) + 1];
    strcpy(this->dataSource, source);
}

MaNiuPlayer::~MaNiuPlayer() {
    DELETE(this->dataSource)
}

void MaNiuPlayer::prepare() {
    pthread_create(&pid_pre, 0, task_init_prepare, this);
}

void MaNiuPlayer::init_prepare() {
    avformat_network_init();

    // TODO 第一步：打开媒体地址（文件路径 、 直播地址）
    // 可以初始为NULL，如果初始为NULL，当执行avformat_open_input函数时，内部会自动申请avformat_alloc_context，这里干脆手动申请
    // 封装了媒体流的格式信息
    this->formatContext = avformat_alloc_context();

    AVDictionary *options = NULL;
    av_dict_set(&options, "timeout", "50000", 0);

    // 1、打开媒体地址(文件地址、直播地址) AVFormatContext  包含了 视频的 信息(宽、高等)
    /**
     * 1.AVFormatContext 二级指针
     * 2.媒体文件路径，或，直播地址   注意：this->data_source; 这样是拿不到的，所以需要把this传递到pVoid
     * 3.输入的封装格式：一般是让ffmpeg自己去检测，所以给了一个0
     * 4.参数
     */
    int ret = avformat_open_input(&this->formatContext, this->dataSource, nullptr, &options);

    // avformat_close_input(&this->formatContext);

    // @@@ 注意：字典使用过后，一定要去释放
    av_dict_free(&options); // 释放字典

    if (ret != 0) {
        LOGE("打开媒体失败:%s", av_err2str(ret));
        callHelper->onError(THREAD_CHILD, FFMPEG_CAN_NOT_OPEN_URL);
        return;
    }

    // TODO 第二步：1.查找媒体中的音视频流的信息  2.给媒体上下文初始化
    ret = avformat_find_stream_info(this->formatContext, 0);

    if (ret < 0) {
        LOGE("查找流失败:%s", av_err2str(ret));
        callHelper->onError(THREAD_CHILD, FFMPEG_CAN_NOT_FIND_STREAMS);
    }

    // TODO 第三步：根据流信息，流的个数，循环查找， 音频流 视频流
    for (int i = 0; i < this->formatContext->nb_streams; ++i) {
        // TODO 第四步：获取媒体流（视频、音频） 从封装格式上下文获取流对象
        AVStream *avStream = this->formatContext->streams[i];
        // TODO 第五步：从stream流中获取解码这段流的参数信息，区分到底是 音频 还是 视频
        AVCodecParameters *codecParameters = avStream->codecpar;

        // TODO 第六步：通过流的编解码参数中编解码ID，来获取当前流的解码器
        AVCodec *avCodec = avcodec_find_decoder(codecParameters->codec_id);
        // 虽然在第六步，已经拿到当前流的解码器了，但可能不支持解码这种解码方式
        if (avCodec == NULL) {
            LOGE("查找解码器失败:%s", av_err2str(ret));
            callHelper->onError(THREAD_CHILD, FFMPEG_FIND_DECODER_FAIL);
            return;
        }

        // TODO 第七步：通过 拿到的解码器，获取解码器上下文
        AVCodecContext *avCodecContext = avcodec_alloc_context3(avCodec);
        if (avCodecContext == NULL) {
            LOGE("创建解码上下文失败:%s", av_err2str(ret));
            callHelper->onError(THREAD_CHILD, FFMPEG_ALLOC_CODEC_CONTEXT_FAIL);
            return;
        }

        // TODO 第八步：给解码器上下文 设置参数 (内部会让编解码器上下文初始化)
        ret = avcodec_parameters_to_context(avCodecContext, codecParameters);
        if (ret < 0) {
            LOGE("设置解码上下文参数失败:%s", av_err2str(ret));
            callHelper->onError(THREAD_CHILD, FFMPEG_CODEC_CONTEXT_PARAMETERS_FAIL);
            return;
        }

        // TODO 第九步：打开解码器
        ret = avcodec_open2(avCodecContext, avCodec, 0);
        if (ret != 0) {
            LOGE("打开解码器失败:%s", av_err2str(ret));
            callHelper->onError(THREAD_CHILD, FFMPEG_OPEN_DECODER_FAIL);
            return;
        }

        // AVStream 媒体流中就可以拿到时间基 (音视频同步)
        AVRational time_base = avStream->time_base;

        LOGE("视频流时间基 分子=%d, 分母=%d", time_base.num, time_base.den); // 视频流时间基 分子=1, 分母=25000

        // TODO 第十步：从编码器参数中获取流类型codec_type
        if (codecParameters->codec_type == AVMEDIA_TYPE_VIDEO) {
            // 获取视频相关的 fps
            // 平均帧率 == 时间基
            AVRational avg_frame_rate = avStream->avg_frame_rate;

            int fps = av_q2d(avg_frame_rate);

            LOGI("MaNiuPlayer::init_prepare() AVStream w=%d, h=%d, fps=%d", avCodecContext->width,
                 avCodecContext->height, fps);

            videoChannel = new VideoChannel(i, avCodecContext, time_base, fps);
            videoChannel->setRenderFrameCallBack(renderFrameCallBack);
        } else if (codecParameters->codec_type == AVMEDIA_TYPE_AUDIO) {
            // 音频
            audioChannel = new AudioChannel(i, avCodecContext, time_base);
        }
    }

    // TODO 第十一步：如果流中 没有音频 也 没有视频
    if (!audioChannel && !videoChannel) {
        LOGE("没有音视频");
        callHelper->onError(THREAD_CHILD, FFMPEG_NO_MEDIA);
        return;
    }

    LOGD("MaNiuPlayer::init_prepare() finish");

    // TODO 第十二步：要么有音频，要么有视频，要么音视频都有
    callHelper->onPrepare(THREAD_CHILD);
}

void MaNiuPlayer::start() {
    isPlaying = 1;

    if (videoChannel) {
        videoChannel->setAudioChannel(audioChannel);
        videoChannel->start();
    }

    if (audioChannel) {
        audioChannel->start();
    }

    // 子线程  把压缩数据 存入到队列里面去
    pthread_create(&pid_play, 0, task_init_start, this);
}

void MaNiuPlayer::init_start() {
    LOGD("MaNiuPlayer::init_start() isPlaying=%d", isPlaying);

    int ret;
    while (isPlaying) {
        // TODO 由于我们的操作是在异步线程，那就好办了，等待（先让消费者消费掉，然后在生产）
        // 下面解决方案：通俗易懂 让生产慢一点，消费了，在生产
        // 内存泄漏点1，解决方案：控制队列大小
        if (audioChannel && audioChannel->packets.size() > 100) {
            //10ms
            av_usleep(1000 * 10);
            continue;
        }
        // 内存泄漏点1，解决方案：控制队列大小
        if (videoChannel && videoChannel->packets.size() > 100) {
            av_usleep(1000 * 10);
            continue;
        }

        // AVPacket 可能是音频 可能是视频, 没有解码的（数据包） 压缩数据AVPacket
        AVPacket *packet = av_packet_alloc();

        ret = av_read_frame(this->formatContext, packet); // 这行代码一执行完毕，packet就有（音视频数据）
        // ret = 0 成功 其他:失败
        if (ret == 0) {
            // stream_index 这一个流的一个序号
            if (audioChannel && packet->stream_index == audioChannel->id) {
                // 如果他们两 相等 说明是音频  音频包

                // 未解码的 音频数据包 加入到队列
                audioChannel->packets.push(packet);

            } else if (videoChannel && packet->stream_index == videoChannel->id) {
                // 如果他们两 相等 说明是视频  视频包

                // 未解码的 视频数据包 加入到队列
                videoChannel->packets.push(packet);
            }
        } else if (ret == AVERROR_EOF) {
            // 读取完成 但是可能还没播放完
            if (audioChannel->packets.empty() && audioChannel->frames.empty()
                && videoChannel->packets.empty() && videoChannel->frames.empty()) {
                break;
            }

            // 为什么这里要让它继续循环 而不是sleep
            // 如果是做直播 ，可以sleep
            // 如果要支持点播(播放本地文件） seek 后退

            LOGD("读取完成 但是可能还没播放完");

        } else {
            // 代表失败了，有问题
            LOGD("代表失败了，有问题");
            break;
        }
    } // while end

    // 最后做释放的工作
    isPlaying = 0;
    videoChannel->stop();
    audioChannel->stop();
}

void MaNiuPlayer::setRenderFrameCallBack(RenderFrameCallback renderFrameCallback) {
    this->renderFrameCallBack = renderFrameCallback;
}

void MaNiuPlayer::stop() {
    this->isPlaying = 0;
    this->callHelper = 0;
    pthread_create(&pid_stop, 0, task_init_stop, this);
}

void *task_init_prepare(void *obj) {
    MaNiuPlayer *player = static_cast<MaNiuPlayer *>(obj);
    if (player) {
        player->init_prepare();
    }
    return 0;
}

void *task_init_start(void *obj) {
    MaNiuPlayer *player = static_cast<MaNiuPlayer *>(obj);
    if (player) {
        player->init_start();
    }

    return 0;
}

void *task_init_stop(void *obj) {
    LOGE("MaNiuPlayer task_stop()");

    MaNiuPlayer *player = static_cast<MaNiuPlayer *>(obj);

    if (player) {
        // 等待prepare结束
        pthread_join(player->pid_pre, 0);

        // 保证 start线程结束
        pthread_join(player->pid_play, 0);

        DELETE(player->videoChannel)
        DELETE(player->audioChannel)

        // 这时候释放就不会出现问题了
        if (player->formatContext) {
            // 先关闭读取 (关闭fileintputstream)
            avformat_close_input(&player->formatContext);
            avformat_free_context(player->formatContext);
            player->formatContext = 0;
        }

        DELETE(player)
    }

    return 0;
}

