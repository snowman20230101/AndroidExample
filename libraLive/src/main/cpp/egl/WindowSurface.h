//
// Created by windy on 2022/8/1.
//

#ifndef ANDROIDEXAMPLE_WINDOWSURFACE_H
#define ANDROIDEXAMPLE_WINDOWSURFACE_H

#include "EglSurfaceBase.h"


class WindowSurface : public EglSurfaceBase {

public:
    WindowSurface(EglCore *eglCore, ANativeWindow *window, bool releaseSurface);

    WindowSurface(EglCore *eglCore, ANativeWindow *window);

    // 释放资源
    void release();

    // 重新创建
    void recreate(EglCore *eglCore);

private:
    ANativeWindow *mSurface;
    bool mReleaseSurface{false};
};


#endif //ANDROIDEXAMPLE_WINDOWSURFACE_H
