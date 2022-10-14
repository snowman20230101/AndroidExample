//
// Created by windy on 2021/12/22.
//

#ifndef LIBRALIVE_COMMONINCLUDE_H
#define LIBRALIVE_COMMONINCLUDE_H

// android 头文件
#include <jni.h>

// 标准库
#include <cstring>
#include <cstdlib>
#include <cstdio>
#include <cstdint>
#include <cassert>
#include <iostream>

#include <malloc.h>

// 自己工程
#include "message_queue.h"
#include "TimeExt.h"
#include "Logger.h"

// openGL 数学
#include <glm.hpp>
#include <gtc/matrix_transform.hpp>
#include <gtc/type_ptr.hpp>

// opengl native 头文件
//#include <GLES2/gl2.h>
#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>

// ffmpeg 头文件
extern "C" {
#include "libavcodec/avcodec.h" // 视频解码器
#include "libavformat/avformat.h" // 封装格式
#include "libavfilter/avfilter.h" // 滤镜
#include "libswscale/swscale.h" // 视频缩放
#include "libavutil/imgutils.h" // 图像处理
#include <libavutil/ffversion.h> // verison
#include <libavutil/time.h> // 时间基
#include "libavutil/samplefmt.h" // 音频采样
#include "libavutil/channel_layout.h" // 音频声道
#include <libavcodec/jni.h> // jni
#include <libswresample/swresample.h> // 音频从采样
#include "libavutil/opt.h"
#include <libavutil/dict.h>
}

// 宏函数
#define DELETE(obj) if(obj) { delete obj; obj = 0; }

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

#endif //LIBRALIVE_COMMONINCLUDE_H
