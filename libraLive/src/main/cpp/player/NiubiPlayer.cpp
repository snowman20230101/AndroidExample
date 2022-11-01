//
// Created by windy on 2021/12/22.
//


#include "NiubiPlayer.h"

/**
 * 线程回调 准备解码器，解封装
 *
 * @param obj
 * @return
 */
void *task_init_prepare(void *obj);

/**
 * 线程回调 开始解码（其实是文件解封装）
 *
 * @param obj
 * @return
 */
void *task_init_start(void *obj);

/**
 * 线程回调 停止解码
 *
 * @param obj
 * @return
 */
void *task_init_stop(void *obj);

NiubiPlayer::NiubiPlayer(const char *source, JavaVM *vm, JNIEnv *env, jobject instance) {
    LOGD("NiubiPlayer::NiubiPlayer()");
    this->callHelper = new JavaCallHelper(vm, env, instance);
    this->dataSource = new char[strlen(source) + 1];
    strcpy(this->dataSource, source);
}

NiubiPlayer::~NiubiPlayer() {
    LOGI("NiubiPlayer::~NiubiPlayer()");
    DELETE(this->dataSource)
    DELETE(this->callHelper)
}

void NiubiPlayer::prepare() {
    pthread_create(&pid_pre, 0, task_init_prepare, this);
}

void NiubiPlayer::init_prepare() {
    avformat_network_init();

    this->formatContext = avformat_alloc_context();
    AVDictionary *options = nullptr;
    av_dict_set(&options, "timeout", "50000", 0);
    LOGE("dataSource is %s", this->dataSource);

    int ret = avformat_open_input(&this->formatContext, this->dataSource, 0, &options);
    av_dict_free(&options); // 释放字典

    if (ret != 0) {
        releaseFormatContext();
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

    this->duration = formatContext->duration / 1000000; // 视频时长（单位：微秒us，转换为秒需要除以1000000）

    fprintf(stdout, "dictionary count: %d\n", av_dict_count(this->formatContext->metadata));
    AVDictionaryEntry *entry = nullptr;
    while ((entry = av_dict_get(this->formatContext->metadata, "", entry, AV_DICT_IGNORE_SUFFIX))) {
        fprintf(stdout, "key: %s, value: %s\n", entry->key, entry->value);
    }

    for (int i = 0; i < this->formatContext->nb_streams; ++i) {
        AVStream *avStream = this->formatContext->streams[i];
        AVCodecParameters *codecParameters = avStream->codecpar;
        AVCodec *avCodec = avcodec_find_decoder(codecParameters->codec_id);
        if (avCodec == nullptr) {
            LOGE("查找解码器失败:%s", av_err2str(ret));
            callHelper->onError(THREAD_CHILD, FFMPEG_FIND_DECODER_FAIL);
            return;
        }

        AVCodecContext *avCodecContext = avcodec_alloc_context3(avCodec);
        if (avCodecContext == nullptr) {
            LOGE("创建解码上下文失败:%s", av_err2str(ret));
            callHelper->onError(THREAD_CHILD, FFMPEG_ALLOC_CODEC_CONTEXT_FAIL);
            return;
        }

        ret = avcodec_parameters_to_context(avCodecContext, codecParameters);
        if (ret < 0) {
            LOGE("设置解码上下文参数失败:%s", av_err2str(ret));
            callHelper->onError(THREAD_CHILD, FFMPEG_CODEC_CONTEXT_PARAMETERS_FAIL);
            return;
        }

        ret = avcodec_open2(avCodecContext, avCodec, 0);
        if (ret != 0) {
            LOGE("打开解码器失败:%s", av_err2str(ret));
            callHelper->onError(THREAD_CHILD, FFMPEG_OPEN_DECODER_FAIL);
            return;
        }

        // AVStream 媒体流中就可以拿到时间基 (音视频同步)
        AVRational time_base = avStream->time_base;
        AVRational codec_time_base = av_stream_get_codec_timebase(avStream);
        const char *codecName = avcodec_get_name(codecParameters->codec_id);

        // // 视频流时间基 分子=1, 分母=25000
        LOGI("NiubiPlayer::init_prepare() codecName=%s, 分子=%d, 分母=%d, duration=%s, %lld, %d, %d",
             codecName,
             time_base.num,
             time_base.den,
             getTimeByHMS((time_t) duration),
             duration,
             codec_time_base.num,
             codec_time_base.den
        );

        av_dump_format(this->formatContext, 0, this->dataSource, 0);

        if (codecParameters->codec_type == AVMEDIA_TYPE_VIDEO) {
            // 获取视频相关的 fps
            // 平均帧率 == 时间基
            AVRational avg_frame_rate = avStream->avg_frame_rate;
            int fps = (int) av_q2d(avg_frame_rate);

            LOGI("NiubiPlayer::init_prepare() AVStream w=%d, h=%d, fps=%d",
                 avCodecContext->width,
                 avCodecContext->height,
                 fps
            );

            videoChannel = new VideoChannel(i, avCodecContext, time_base, fps);
            videoChannel->setRenderFrameCallBack(renderFrameCallBack);
        } else if (codecParameters->codec_type == AVMEDIA_TYPE_AUDIO) {
            // 音频
            audioChannel = new AudioChannel(i, avCodecContext, time_base);
        }
    }

    if (!audioChannel && !videoChannel) {
        LOGE("没有音视频");
        callHelper->onError(THREAD_CHILD, FFMPEG_NO_MEDIA);
        return;
    }

    LOGD("NiubiPlayer::init_prepare() successful.");

    callHelper->onPrepare(THREAD_CHILD);
}

void NiubiPlayer::start() {
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

void NiubiPlayer::init_start() {
    LOGD("NiubiPlayer::init_start() isPlaying=%d", isPlaying);

    int ret;
    while (isPlaying) {
        if (audioChannel && audioChannel->packets.size() > 100) {
            // 10ms
            av_usleep(1000 * 10);
//            LOGD("NiubiPlayer::init_start() audio packets size is %d 多了，睡一会儿",
//                 audioChannel->packets.size());
            continue;
        }
        if (videoChannel && videoChannel->packets.size() > 100) {
            av_usleep(1000 * 10);
//            LOGD("NiubiPlayer::init_start() video packets size is %d 多了，睡一会儿",
//                 videoChannel->packets.size());
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
                audioChannel->packets.push(packet);

            } else if (videoChannel && packet->stream_index == videoChannel->id) {
                // 如果他们两 相等 说明是视频  视频包
                videoChannel->packets.push(packet);
                LOGD("video key flag=%d", packet->flags);
            }
        } else if (ret == AVERROR_EOF) {
            // 读取完成 但是可能还没播放完
            BaseChannel::releaseAvPacket(packet);
            if (audioChannel->packets.empty() && audioChannel->frames.empty()
                && videoChannel->packets.empty() && videoChannel->frames.empty()) {
                LOGE("NiubiPlayer::init_start() packets empty. frame empty. ret=%s",
                     av_err2str(ret));
                break;
            }

            // 为什么这里要让它继续循环 而不是sleep
            // 如果是做直播 ，可以sleep
            // 如果要支持点播(播放本地文件） seek 后退
            LOGD("读取完成 但是可能还没播放完");
        } else {
            // 代表失败了，有问题
            LOGD("代表失败了，有问题");
            BaseChannel::releaseAvPacket(packet);
            break;
        }
    } // while end

    LOGE("NiubiPlayer::init_start() finished.");

    // 最后做释放的工作
    isPlaying = 0;
    videoChannel->stop();
    audioChannel->stop();
}

void NiubiPlayer::setRenderFrameCallBack(RenderFrameCallback renderFrameCallback) {
    this->renderFrameCallBack = renderFrameCallback;
}

void NiubiPlayer::stop() {
    this->isPlaying = 0;
    this->callHelper = 0;
    pthread_create(&pid_stop, 0, task_init_stop, this);
}

void NiubiPlayer::init_stop() {
    LOGD("NiubiPlayer::init_stop() 释放播放器资源");
    // 等待 prepare 结束
    pthread_join(pid_pre, 0);

    // 保证 start 线程结束
    pthread_join(pid_play, 0);

    DELETE(videoChannel)
    DELETE(audioChannel)
    DELETE(callHelper)

    // 释放封装格式上下文
    releaseFormatContext();
}

void NiubiPlayer::releaseFormatContext() {
    if (this->formatContext) {
        avformat_close_input(&this->formatContext);
        avformat_free_context(this->formatContext);
        this->formatContext = nullptr;
    }
}

void *task_init_prepare(void *obj) {
    auto *player = static_cast<NiubiPlayer *>(obj);
    if (player) {
        player->init_prepare();
    }
    return nullptr;
}

void *task_init_start(void *obj) {
    auto *player = static_cast<NiubiPlayer *>(obj);
    if (player) {
        player->init_start();
    }
    return nullptr;
}

void *task_init_stop(void *obj) {
    auto *player = static_cast<NiubiPlayer *>(obj);
    if (player) {
        player->init_stop();
    }
    return 0;
}

