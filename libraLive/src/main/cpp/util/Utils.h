//
// Created by windy on 2022/9/7.
//

#ifndef ANDROIDEXAMPLE_UTILS_H
#define ANDROIDEXAMPLE_UTILS_H

#include "client/linux/handler/minidump_descriptor.h"
#include "client/linux/handler/exception_handler.h"

class Utils {
public:
    /**
     * 初始化 breakPad 用于抓取c++崩溃
     * @param path
     */
    static void initBreakPad(const char *path);
};

#endif //ANDROIDEXAMPLE_UTILS_H
