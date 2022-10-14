//
// Created by windy on 2022/9/16.
//

#ifndef ANDROIDEXAMPLE_TIMEEXT_H
#define ANDROIDEXAMPLE_TIMEEXT_H

#include <sys/time.h>
#include <time.h>

/**
 * 获取当前时间
 *
 * @return 返回毫秒 ms
 */
static long long GetSysCurrentTime() {
    struct timeval time;
    gettimeofday(&time, NULL);
    // tv_usec 为 微妙， tv_sec 为 秒
    long long curTime = ((long long) (time.tv_sec)) * 1000 + time.tv_usec / 1000;

    return curTime;
}

static const char *getTimeByYMD() {
    time_t current = GetSysCurrentTime() / 1000;
    char s[100];
    tm *p = gmtime(&current); // UTC
    tm *lp = localtime(&current); // CST
    tm tmp = {0};
    // 这里是保护，空指针
    if (p) {
        tmp = *p;
    }

    strftime(s, sizeof(s), "%Y-%m-%d", &tmp);
    return s;
}

/**
 * 格式化当前时间
 *
 * @param sec 时间（秒）
 * @return const char * 返回当前格式化的时间
 */
static const char *getTimeByHMS(time_t sec) {
    char s[100];
    struct tm *p = gmtime(&sec);
//    tm *lp = localtime(&sec);
    struct tm tmp = {0};
    // 这里是保护，空指针
    if (p) {
        tmp = *p;
    }

    if (sec >= 3600) {
        strftime(s, sizeof(s), "%H:%M:%S", &tmp);
    } else {
        strftime(s, sizeof(s), "%M:%S", &tmp);
    }
    return s;
}

static std::string CC_FORMAT(const char *fmt, ...) {
    int size = 1024;

    std::string str;
    va_list ap;
    for (int j = 0; j < 8; ++j) {     // Maximum two passes on a POSIX system...
        str.resize(size);
        va_start(ap, fmt);
        int n = vsnprintf((char *) str.data(), size, fmt, ap);
        va_end(ap);
        if (n >= 0 && n < size) {  // Everything worked
            str.resize(n);
            return str;
        }
        if (n > -1)  // Needed size returned
            size = n + 1;   // For null char
        else
            size = (j + 1) * 32 * 1024;      // Guess at a larger size (OS specific)
    }

    str.clear();
    return str;
}

#endif //ANDROIDEXAMPLE_TIMEEXT_H
