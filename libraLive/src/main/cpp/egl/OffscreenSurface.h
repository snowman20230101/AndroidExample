//
// Created by windy on 2022/8/1.
//

#ifndef ANDROIDEXAMPLE_OFFSCREENSURFACE_H
#define ANDROIDEXAMPLE_OFFSCREENSURFACE_H

#include "EglSurfaceBase.h"

class OffscreenSurface : public EglSurfaceBase {
public:
    OffscreenSurface(EglCore *eglCore, int width, int height);

    void release();
};


#endif //ANDROIDEXAMPLE_OFFSCREENSURFACE_H
