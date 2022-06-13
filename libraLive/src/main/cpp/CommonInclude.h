//
// Created by windy on 2021/12/22.
//

#ifndef LIBRALIVE_COMMONINCLUDE_H
#define LIBRALIVE_COMMONINCLUDE_H

#include <jni.h>

#include <cstring>
#include "message_queue.h"

#include <android/log.h>

#define TAG_FFMPEG "FFMPEG"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG_FFMPEG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG_FFMPEG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG_FFMPEG, __VA_ARGS__)


// ffmpeg 头文件
extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavfilter/avfilter.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
#include <libavutil/ffversion.h>
#include <libavutil/time.h>
#include <libavcodec/jni.h>
#include <libswresample/swresample.h>
}

// 宏函数
#define DELETE(obj) if(obj){ delete obj; obj = 0; }

// 标记线程 因为子线程需要attach
#define THREAD_MAIN 1
#define THREAD_CHILD 2

// 错误代码
// 打不开视频
#define FFMPEG_CAN_NOT_OPEN_URL 1
// 找不到流媒体
#define FFMPEG_CAN_NOT_FIND_STREAMS 2
// 找不到解码器
#define FFMPEG_FIND_DECODER_FAIL 3
// 无法根据解码器创建上下文
#define FFMPEG_ALLOC_CODEC_CONTEXT_FAIL 4
// 根据流信息 配置上下文参数失败
#define FFMPEG_CODEC_CONTEXT_PARAMETERS_FAIL 6
// 打开解码器失败
#define FFMPEG_OPEN_DECODER_FAIL 7
// 没有音视频
#define FFMPEG_NO_MEDIA 8

#define COCOS2D_DEBUG 1


#if defined(__GNUC__) && (__GNUC__ >= 4)
#define CC_FORMAT_PRINTF(formatPos, argPos) __attribute__((__format__(printf, formatPos, argPos)))
#elif defined(__has_attribute)
#if __has_attribute(format)
#define CC_FORMAT_PRINTF(formatPos, argPos) __attribute__((__format__(printf, formatPos, argPos)))
#endif // __has_attribute(format)
#else
#define CC_FORMAT_PRINTF(formatPos, argPos)
#endif

#if COCOS2D_DEBUG == 1
#define CCLOG(format, ...)      cocos2d::log(format, ##__VA_ARGS__)
#define CCLOGERROR(format, ...)  cocos2d::log(format, ##__VA_ARGS__)
#define CCLOGINFO(format, ...)   do {} while (0)
#define CCLOGWARN(...) __CCLOGWITHFUNCTION(__VA_ARGS__)
#endif // COCOS2D_DEBUG


static const int MAX_LOG_LENGTH = 16 * 1024;

void log(const char *format, ...) CC_FORMAT_PRINTF(1, 2);


class CommonInclude {
public:
    CommonInclude();
};


#endif //LIBRALIVE_COMMONINCLUDE_H
