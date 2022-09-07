//
// Created by windy on 2022/8/1.
//

#ifndef ANDROIDEXAMPLE_EGLSURFACEBASE_H
#define ANDROIDEXAMPLE_EGLSURFACEBASE_H


#include "EglCore.h"
#include <CommonInclude.h>

class EglSurfaceBase {

public:
    EglSurfaceBase(EglCore *eglCore);

    // 创建窗口Surface
    void createWindowSurface(ANativeWindow *nativeWindow);

    // 创建离屏Surface
    void createOffscreenSurface(int width, int height);

    // 获取宽度
    int getWidth();

    // 获取高度
    int getHeight();

    // 释放EGLSurface
    void releaseEglSurface();

    // 切换到当前上下文
    void makeCurrent();

    // 交换缓冲区，显示图像
    bool swapBuffers();

    // 设置显示时间戳
    void setPresentationTime(long nsecs);

    // 获取当前帧缓冲
    __attribute__((unused)) __attribute__((unused)) char *getCurrentFrame();

protected:
    EglCore *mEglCore;
    EGLSurface mEglSurface;
    int mWidth{-1};
    int mHeight{-1};
};


#endif //ANDROIDEXAMPLE_EGLSURFACEBASE_H
