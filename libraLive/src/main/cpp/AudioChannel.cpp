//
// Created by windy on 2021/12/23.
//

#include "AudioChannel.h"

// 声明并且实现
void *audio_decode(void *args);

void *audio_play(void *args);

AudioChannel::AudioChannel(int id, AVCodecContext *avCodecContext, AVRational time_base)
        : BaseChannel(id, avCodecContext, time_base) {
    this->id = id;
    this->mAvCodecContext = avCodecContext;
    this->time_base = time_base;

    // 播放的参数 必须固定，声卡要求（输出 音频重要参数，必须固定）
    // 40000 输入   3000输入
    // 输出必须规定，否则，声卡不同意


    // 初始化 缓冲区 out_buffers
    // 如何去定义缓冲区？
    // 答：根据数据类型 44100  和  16bits  和  2声道

    // 可以是写死的方式
    // out_buffers_size = 44100 * 2 * 2;

    out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO); // 声道
    out_sample_size = av_get_bytes_per_sample(AV_SAMPLE_FMT_S16); // 输出缓冲区位数
    out_sample_rate = 44100; // 采样

    // 2*44.1kHz*16bit=1.411Mbit/s
    int out_buffers_size = out_sample_rate * out_sample_size * out_channels;

    // 缓冲区是拿到三者  通道数 * 采用率 * s16
    out_buffers = static_cast<uint8_t *>(malloc(out_buffers_size)); // unsigned char*
    memset(out_buffers, 0, out_buffers_size);

    // 必须把 上面的配置 设置到 转换上下文
    // swr_ctx = swr_alloc();
    // 0 + 输出声道+ 输出采样位+输出采样率 + 输入的3个参数
    swrContext = swr_alloc_set_opts(0,
                                    AV_CH_LAYOUT_STEREO,
                                    AV_SAMPLE_FMT_S16,
                                    out_sample_rate,
                                    mAvCodecContext->channel_layout,
                                    mAvCodecContext->sample_fmt,
                                    mAvCodecContext->sample_rate,
                                    0,
                                    0
    );

    // 初始化一下 转换上下文 目的：防止有 吃吃吃的声音
    swr_init(swrContext);
}

AudioChannel::~AudioChannel() {
    if (out_buffers) {
        free(out_buffers);
        out_buffers = 0;
    }
}

void AudioChannel::start() {
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

    // 48000HZ 8位 =》 44100 16位
    // 重采样
    // 假设我们输入了10个数据 ，swrContext转码器 这一次处理了8个数据
    // 那么如果不加delays(上次没处理完的数据) , 积压
    int64_t delays = swr_get_delay(swrContext, frame->sample_rate);

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

    // 获取 frame 的一个相对播放时间 （相对开始播放）
    // 获得 相对播放这一段数据的秒数
    this->audioTime = frame->best_effort_timestamp * av_q2d(time_base); // frame->pts

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