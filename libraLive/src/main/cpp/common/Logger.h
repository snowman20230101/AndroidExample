//
// Created by windy on 2022/9/16.
//

#ifndef ANDROIDEXAMPLE_LOGGER_H
#define ANDROIDEXAMPLE_LOGGER_H

#include <jni.h>
#include <android/log.h>

#include "TimeExt.h"

#define LOG_TAG "FFMPEG"

// 定义输出缓冲区大小
static const int MAX_LOG_LENGTH = 16 * 1024;

// 定义日志文件地址
static char appLogPath[100];

static bool isSaveLog = false;

// 定义 printf
#if defined(__GNUC__) && (__GNUC__ >= 4)
#define CC_FORMAT_PRINTF(formatPos, argPos) __attribute__((__format__(printf, formatPos, argPos)))
#elif defined(__has_attribute)
#if __has_attribute(format)
#define CC_FORMAT_PRINTF(formatPos, argPos) __attribute__((__format__(printf, formatPos, argPos)))
#endif // __has_attribute(format)
#else
#define CC_FORMAT_PRINTF(formatPos, argPos)
#endif

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/**
 *
 * @param format
 * @param ...
 */
static void log(const char *format, ...) CC_FORMAT_PRINTF(1, 2);

/**
 *
 * @param level
 * @param tag
 * @param format
 * @param args
 */
static void _log_native_print(android_LogPriority level, const char *tag,
                              const char *format, va_list args);

// #################################################################################################################################################

// 红函数 找文件名
#define __SHORT_FORM_OF_FILE__ \
(strrchr(__FILE__,'/') \
? strrchr(__FILE__,'/') + 1 \
: __FILE__ \
)

#define CCLOGTIME(s) \
{                \
    timeval tv;      \
    gettimeofday(&tv, NULL) \
    log("%s(%s:%d) TIME %u.%.6u " s, __FUNCTION__, __SHORT_FORM_OF_FILE__, __LINE__, tv.tv_sec, tv.tv_usec); \
}

#define CCLOGTIMEF(format, ...) \
{ \
    timeval tv; \
    gettimeofday(&tv, NULL); \
    std::string sFmt="%s(%s:%d) TIME %u.%.6u "; \
    sFmt += format; \
    log(sFmt.c_str(), __FUNCTION__, __SHORT_FORM_OF_FILE__, __LINE__, tv.tv_sec, tv.tv_usec, ##__VA_ARGS__); \
}

// 打印方法名字，所在文件名，日志写在具体行数
#define CCLOGFUNC(s) \
{ \
    log("%s(%s:%d) " s, __FUNCTION__, __SHORT_FORM_OF_FILE__, __LINE__); \
}

// 打印方法名字，所在文件名，日志写在具体行数
#define CCLOGFUNCF(format, ...) \
{ \
    std::string sFmt="%s(%s:%d) "; \
    sFmt += format; \
    log(sFmt.c_str(), __FUNCTION__, __SHORT_FORM_OF_FILE__, __LINE__, ##__VA_ARGS__); \
}

#define CClog(format, ...) \
{ \
    log_time_(format, ##__VA_ARGS__); \
}

// #################################################################################################################################################

/**
 * 监控函数运行时间
 */
// -----监控函数运行时间------
#define FUN_BEGIN_TIME(FUN) \
{ \
    LOGE("%s:%s func start", __FILE__, FUN); \
    long long t0 = GetSysCurrentTime();

#define FUN_END_TIME(FUN) \
    long long t1 = GetSysCurrentTime(); \
    LOGE("%s:%s func cost time %ld ms", __FILE__, FUN, (long)(t1 - t0)); \
}
// -----监控函数运行时间------

/**
 * 自定义监控函数
 */
// -----自定义监控函数------
#define BEGIN_TIME(FUN) \
{ \
    LOGE("%s func start", FUN); \
    long long t0 = GetSysCurrentTime();

#define END_TIME(FUN) \
    long long t1 = GetSysCurrentTime(); \
    LOGE("%s func cost time %ld ms", FUN, (long)(t1 - t0)); \
}
// -----自定义监控函数------

/**
 *  检测GL Error。 并且直接打印日志
 */
#define GO_CHECK_GL_ERROR(...)  LOGE("CHECK_GL_ERROR %s glGetError = %d, line = %d, ",  __FUNCTION__, glGetError(), __LINE__)

/**
 * 打印日志系统
 *
 * @param format
 * @param ... 可变参数
 */
static void log(const char *format, ...) {
    int bufferSize = MAX_LOG_LENGTH;
    char *buf;
    int nret;
    va_list args;
    do {
        buf = new(std::nothrow) char[bufferSize];
        if (buf == nullptr)
            return;
        va_start(args, format);
        nret = vsnprintf(buf, bufferSize - 3, format, args);
        va_end(args);

        if (nret >= 0) {
            if (nret <= bufferSize - 3) {// success, so don't need to realloc
                break;
            } else {
                bufferSize = nret + 3;
                delete[] buf;
            }
        } else {
            // < 0
            bufferSize *= 2;
            delete[] buf;
        }
    } while (true);
    buf[nret] = '\n';
    buf[++nret] = '\0';
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%s", buf);
    delete[] buf;
}

static void _log_native_print(android_LogPriority level, const char *tag,
                              const char *format, va_list args) {
    char buf[MAX_LOG_LENGTH]; // 栈
    timeval now;
    struct tm *time;
    gettimeofday(&now, nullptr);
    time = localtime((&now.tv_sec));

    int len;
    if (time == nullptr) {
        return;
    }

    if (isSaveLog) {
        len = snprintf(buf, sizeof(buf) - 1, "[%s] [%02d:%02d:%02d.%03d] [%08lx] ",
                       getTimeByYMD(),
                       time->tm_hour,
                       time->tm_min,
                       time->tm_sec,
                       int(now.tv_usec / 1000),
                       pthread_self()
        );
    }

    if (len >= 0 && len < sizeof(buf)) {
        vsnprintf(buf + len, sizeof(buf) - len - 1, format, args);
    }

    // 之前的空间应该已经处理好了。
    strcat(buf, "\n");

    if (isSaveLog) {
        std::string path = CC_FORMAT("%s/okay_log_%d_%d.log",
                                     appLogPath,
                                     (time->tm_mon + 1),
                                     time->tm_mday
        );

        FILE *fd = fopen(path.c_str(), "a");
        if (fd) {
            fputs(buf, fd);
            fflush(fd);
            fclose(fd);
        }
    }

    __android_log_print(level, tag, "%s", buf);
}

//static void LOGD(const char *format, ...) {
//    va_list args;
//    va_start(args, format);
//    _log_native_print(ANDROID_LOG_DEBUG, LOG_TAG, format, args);
//    va_end(args);
//}
//
//static void LOGI(const char *format, ...) {
//    va_list args;
//    va_start(args, format);
//    _log_native_print(ANDROID_LOG_INFO, LOG_TAG, format, args);
//    va_end(args);
//}
//
//static void LOGE(const char *format, ...) {
//    va_list args;
//    va_start(args, format);
//    _log_native_print(ANDROID_LOG_ERROR, LOG_TAG, format, args);
//    va_end(args);
//}

#endif //ANDROIDEXAMPLE_LOGGER_H
