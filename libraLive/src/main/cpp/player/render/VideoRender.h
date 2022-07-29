//
// Created by windy on 2022/7/29.
//

#ifndef ANDROIDEXAMPLE_VIDEORENDER_H
#define ANDROIDEXAMPLE_VIDEORENDER_H

#include <stdint.h>

struct NativeImage {
    uint8_t ppPlanal[3];
};


class VideoRender {
public:
    virtual ~VideoRender() {}

    virtual void Init(int videoWidth, int videoHeight, int *dstSize) = 0;

    virtual void RenderVideoFrame(NativeImage *pImage) = 0;

    virtual void UnInit() = 0;
};

#endif //ANDROIDEXAMPLE_VIDEORENDER_H
