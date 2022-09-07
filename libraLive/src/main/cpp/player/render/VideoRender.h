//
// Created by windy on 2022/7/29.
//

#ifndef ANDROIDEXAMPLE_VIDEORENDER_H
#define ANDROIDEXAMPLE_VIDEORENDER_H

#include <stdint.h>
#include <ImageDef.h>

#define VIDEO_RENDER_OPENGL             0
#define VIDEO_RENDER_ANWINDOW           1
#define VIDEO_RENDER_3D_VR              2

/**
 * 基类用于绘制视频
 */
class VideoRender {
public:
    VideoRender(int type) {
        m_RenderType = type;
    }

    virtual ~VideoRender() {}

    virtual void Init(int videoWidth, int videoHeight, int *dstSize) = 0;

    virtual void RenderVideoFrame(NativeImage *pImage) = 0;

    virtual void UnInit() = 0;

    int GetRenderType() {
        return m_RenderType;
    }

private:
    // 绘制类型
    int m_RenderType = VIDEO_RENDER_ANWINDOW;
};

#endif //ANDROIDEXAMPLE_VIDEORENDER_H
