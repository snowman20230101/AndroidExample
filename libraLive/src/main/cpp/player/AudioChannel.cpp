//
// Created by windy on 2021/12/23.
//

#include "AudioChannel.h"

// 声明并且实现
void *audio_decode(void *args);

void *audio_play(void *args);

static int get_format_from_sample_fmt(const char **fmt,
                                      enum AVSampleFormat sample_fmt) {
    int i;
    struct sample_fmt_entry {
        enum AVSampleFormat sample_fmt;
        const char *fmt_be, *fmt_le;
    } sample_fmt_entries[] = {
            {AV_SAMPLE_FMT_U8,  "u8",    "u8"},
            {AV_SAMPLE_FMT_S16, "s16be", "s16le"},
            {AV_SAMPLE_FMT_S32, "s32be", "s32le"},
            {AV_SAMPLE_FMT_FLT, "f32be", "f32le"},
            {AV_SAMPLE_FMT_DBL, "f64be", "f64le"},
    };
    *fmt = NULL;

    for (i = 0; i < FF_ARRAY_ELEMS(sample_fmt_entries); i++) {
        struct sample_fmt_entry *entry = &sample_fmt_entries[i];
        if (sample_fmt == entry->sample_fmt) {
            *fmt = AV_NE(entry->fmt_be, entry->fmt_le);
            return 0;
        }
    }

    fprintf(stderr,
            "Sample format %s not supported as output format\n",
            av_get_sample_fmt_name(sample_fmt));
    return AVERROR(EINVAL);
}

/**
 * Fill dst buffer with nb_samples, generated starting from t.
 */
static void fill_samples(double *dst, int nb_samples, int nb_channels, int sample_rate, double *t) {
    int i, j;
    double tincr = 1.0 / sample_rate, *dstp = dst;
    const double c = 2 * M_PI * 440.0;

    /* generate sin tone with 440Hz frequency and duplicated channels */
    for (i = 0; i < nb_samples; i++) {
        *dstp = sin(c * *t);
        for (j = 1; j < nb_channels; j++)
            dstp[j] = dstp[0];
        dstp += nb_channels;
        *t += tincr;
    }
}

AudioChannel::AudioChannel(int id, AVCodecContext *avCodecContext, AVRational time_base)
        : BaseChannel(id, avCodecContext, time_base) {
    this->id = id;
    this->mAvCodecContext = avCodecContext;
    this->time_base = time_base;

    out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO); // 输出声道
    out_sample_size = av_get_bytes_per_sample(AV_SAMPLE_FMT_S16); // 输出缓冲区位数
    out_sample_rate = 44100; // 采样

//
//    // 2*44.1kHz*16bit=1.411Mbit/s
    int out_buffers_size = out_sample_rate * out_sample_size * out_channels;
//
//    // 缓冲区是拿到三者  通道数 * 采用率 * s16
    out_buffers = static_cast<uint8_t *>(malloc(out_buffers_size)); // unsigned char*
    memset(out_buffers, 0, out_buffers_size);

    // 申请上下文
    swrContext = swr_alloc_set_opts(swrContext,
                                    AV_CH_LAYOUT_STEREO,
                                    AV_SAMPLE_FMT_S16,
                                    out_sample_rate,
                                    mAvCodecContext->channel_layout,
                                    mAvCodecContext->sample_fmt,
                                    mAvCodecContext->sample_rate,
                                    0,
                                    0
    );

    // 初始化一下 转换上下文
    int ret = swr_init(swrContext);
    if (ret < 0) {
        LOGE("Failed to initialize the swr_init context ret=%s", av_err2str(ret));
    }

    // converted input samples 转换输入样本
//    max_dst_nb_samples = dst_nb_samples = av_rescale_rnd(
//            src_nb_samples,
//            out_sample_rate,
//            mAvCodecContext->sample_rate,
//            AV_ROUND_UP
//    );
//
//    LOGE("AudioChannel: max_dst_nb_samples=%lld", max_dst_nb_samples);
//
//    // 输 出
//    av_samples_alloc_array_and_samples(
//            &dst_data,
//            &dst_linesize,
//            out_channels,
//            (int) dst_nb_samples,
//            dst_sample_fmt,
//            0
//    );
//
//    LOGE("AudioChannel: dst_linesize=%d", dst_linesize);
}

AudioChannel::~AudioChannel() {
    if (out_buffers) {
        free(out_buffers);
        out_buffers = 0;
    }

//    if (dst_data) {
//        av_free(dst_data);
//        dst_data = 0;
//    }
}

void AudioChannel::start() {
    LOGE("AudioChannel::start()");
    isPlaying = 1;

    // 设置为播放状态
    packets.gotoWork();
    frames.gotoWork();

    //1 、解码
    pthread_create(&pid_audio_decode, 0, audio_decode, this);
    //2、 播放
    pthread_create(&pid_audio_play, 0, audio_play, this);
}

void AudioChannel::decode() {
    LOGE("AudioChannel::decode()");
    AVPacket *packet = 0;

    while (isPlaying) {
        // 生产快  消费慢
        // 消费速度比生成速度慢（生成100，只消费10个，这样队列会爆）
        // 内存泄漏点1，解决方案：控制队列大小
        if (isPlaying && frames.size() > 100) {
            // 休眠 等待队列中的数据被消费
            av_usleep(10 * 1000);
            continue;
        }

        // 取出一个数据包
        int ret = packets.pop(packet);

        // 如果停止播放，跳出循环, 出了循环，就要释放
        if (!isPlaying) {
            break;
        }

        // 取出失败
        if (!ret) {
            continue;
        }

        // 走到这里，就证明，取到了待解码的音频数据包（AAC）

        // 把包丢给解码器
        ret = avcodec_send_packet(mAvCodecContext, packet);

        releaseAvPacket(&packet);
        // 重试
        if (ret != 0) {
            // 失败了（给”解码器上下文“发送Packet失败）
            break;
        }

        AVFrame *frame = av_frame_alloc(); // AVFrame 拿到解码后的原始数据包

        ret = avcodec_receive_frame(mAvCodecContext, frame); // 从解码器中读取 解码后的数据包 AVFrame

        // 需要更多的数据才能够进行解码 eagain
        if (ret == AVERROR(EAGAIN)) {
            // 在此状态下，输出不可用——用户必须尝试发送新的输入
            LOGE("output is not available in this state - "
                 "user must try to send new input ret=%s", av_err2str(ret));
            continue;
        } else if (ret == AVERROR_EOF) {
            LOGE("the decoder has been fully flushed, and there will be"
                 "no more output frames. ret=%s", av_err2str(ret));
            continue;
        } else if (ret == AVERROR(EINVAL)) {
            LOGE("codec not opened, or it is an encoder. ret=%s", av_err2str(ret));
            continue;
        } else if (ret == AVERROR_INPUT_CHANGED) {
            LOGE("current decoded frame has changed parameters"
                 " with respect to first decoded frame. Applicable"
                 " when flag AV_CODEC_FLAG_DROPCHANGED is set. ret=%s", av_err2str(ret));
            continue;
        } else if (ret != 0) {
            LOGE("AudioChannel::decode() error. ret=%s", av_err2str(ret));
            break;
        }

        // 再开一个线程 来播放 (流畅度)
        frames.push(frame);
    }

    releaseAvPacket(&packet);
}

int AudioChannel::getPcm() {
    int pcm_data_size = 0;

    AVFrame *frame = 0;

    int ret = frames.pop(frame);

    if (!isPlaying) {
        if (ret) {
            releaseAvFrame(&frame);
        }

        return pcm_data_size;
    }

    // 重采样
    int64_t delays = swr_get_delay(swrContext, frame->sample_rate);

    // 转换输出
//    dst_nb_samples = av_rescale_rnd(
//            delays + frame->nb_samples,
//            out_sample_rate,
//            frame->sample_rate,
//            AV_ROUND_UP
//    );
//
//    if (dst_nb_samples > max_dst_nb_samples) {
//        LOGE("AudioChannel::getPcm() 缓冲区变了，重新调整");
//        av_freep(&dst_data[0]);
//        int ret = av_samples_alloc(dst_data, &dst_linesize, out_channels, (int) dst_nb_samples,
//                                   dst_sample_fmt, 1);
//        if (ret < 0) {
//            LOGE("AudioChannel::getPcm() Error av_samples_alloc ...");
//        }
//
//        max_dst_nb_samples = dst_nb_samples;
//    }
//
//    // 上下文+输出缓冲区+输出缓冲区能接受的最大数据量+输入数据+输入数据个数
//    // 返回 每一个声道的输出数据个数
//    int samples = swr_convert(swrContext,
//                              dst_data,
//                              (int) dst_nb_samples,
//                              (const uint8_t **) frame->data,
//                              frame->nb_samples
//    );
//
//    // 获取给定音频参数所需的缓冲区大小
//    pcm_data_size = av_samples_get_buffer_size(
//            &dst_linesize,
//            out_channels,
//            samples,
//            dst_sample_fmt,
//            1
//    );


    // 将 nb_samples 个数据 由 sample_rate采样率转成 44100 后 返回多少个数据
    // 10  个 48000 = nb 个 44100
    // AV_ROUND_UP : 向上取整 1.1 = 2
    int64_t max_samples = av_rescale_rnd(delays + frame->nb_samples,
                                         out_sample_rate,
                                         frame->sample_rate,
                                         AV_ROUND_UP
    );

    // 上下文+输出缓冲区+输出缓冲区能接受的最大数据量+输入数据+输入数据个数
    // 返回 每一个声道的输出数据个数
    int samples = swr_convert(swrContext,
                              &out_buffers,
                              max_samples,
                              (const uint8_t **) frame->data,
                              frame->nb_samples
    );


    // 获得   samples 个   * 2 声道 * 2字节（16位）
    pcm_data_size = samples * out_sample_size * out_channels;

    // 获得 相对播放这一段数据的秒数
    this->audioTime = frame->best_effort_timestamp * av_q2d(time_base);

    releaseAvFrame(&frame);

    return pcm_data_size;
}

void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    AudioChannel *audioChannel = static_cast<AudioChannel *>(context);
    //获得pcm 数据 多少个字节 out_buffers
    int dataSize = audioChannel->getPcm();
    if (dataSize > 0) {
        // 接收16位数据
        (*bq)->Enqueue(bq, audioChannel->out_buffers, dataSize);
    }
}

void AudioChannel::init_start() {
    /**
    * 1、创建引擎并获取引擎接口
    */
    SLresult result;
    // 1.1 创建引擎 SLObjectItf engineObject
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    // 1.2 初始化引擎  init
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    // 1.3 获取引擎接口SLEngineItf engineInterface
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineInterface);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }

    /**
     * 2、设置混音器
     */
    // 2.1 创建混音器SLObjectItf outputMixObject
    result = (*engineInterface)->CreateOutputMix(engineInterface, &outputMixObject, 0, 0, 0);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }

    // 2.2 初始化混音器outputMixObject
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }

    /**
     * 3、创建播放器
     */
    //3.1 配置输入声音信息
    //创建buffer缓冲类型的队列 2个队列
    SLDataLocator_AndroidSimpleBufferQueue android_queue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                            2};
    //pcm数据格式
    //pcm+2(双声道)+44100(采样率)+ 16(采样位)+16(数据的大小)+LEFT|RIGHT(双声道)+小端数据
    SLDataFormat_PCM pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1, SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                            SL_BYTEORDER_LITTLEENDIAN};

    // 数据源 将上述配置信息放到这个数据源中
    SLDataSource slDataSource = {&android_queue, &pcm};

    //3.2  配置音轨(输出)
    //设置混音器
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&outputMix, NULL};
    //需要的接口  操作队列的接口
    const SLInterfaceID ids[1] = {SL_IID_BUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    //3.3 创建播放器
    (*engineInterface)->CreateAudioPlayer(engineInterface, &bqPlayerObject, &slDataSource,
                                          &audioSnk, 1,
                                          ids, req);
    //初始化播放器
    (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);

    //得到接口后调用  获取Player接口
    (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerInterface);


    /**
     * 4、设置播放回调函数
     */
    //获取播放器队列接口
    (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE,
                                    &bqPlayerBufferQueueInterface);
    //设置回调
    (*bqPlayerBufferQueueInterface)->RegisterCallback(bqPlayerBufferQueueInterface,
                                                      bqPlayerCallback, this);
    /**
     * 5、设置播放状态
     */
    (*bqPlayerInterface)->SetPlayState(bqPlayerInterface, SL_PLAYSTATE_PLAYING);
    /**
     * 6、手动激活一下这个回调
     */
    bqPlayerCallback(bqPlayerBufferQueueInterface, this);
}

void AudioChannel::stop() {
    LOGE("AudioChannel::stop()");

    isPlaying = 0;
    packets.backWork();
    frames.backWork();
    pthread_join(pid_audio_decode, 0);
    pthread_join(pid_audio_play, 0);

    if (swrContext) {
        swr_free(&swrContext);
        swrContext = 0;
    }

    // 释放播放器
    if (bqPlayerObject) {
        (*bqPlayerObject)->Destroy(bqPlayerObject);
        bqPlayerObject = 0;
        bqPlayerInterface = 0;
        bqPlayerBufferQueueInterface = 0;
    }

    // 释放混音器
    if (outputMixObject) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = 0;
    }

    // 释放引擎
    if (engineObject) {
        (*engineObject)->Destroy(engineObject);
        engineObject = 0;
        engineInterface = 0;
    }
}

void *audio_decode(void *args) {
    AudioChannel *audioChannel = static_cast<AudioChannel *>(args);
    audioChannel->decode();
    return 0;
}

void *audio_play(void *args) {
    AudioChannel *audioChannel = static_cast<AudioChannel *>(args);
    audioChannel->init_start();
    return 0;
}