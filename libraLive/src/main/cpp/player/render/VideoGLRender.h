//
// Created by windy on 2022/7/29.
//

#ifndef ANDROIDEXAMPLE_VIDEOGLRENDER_H
#define ANDROIDEXAMPLE_VIDEOGLRENDER_H

#include "VideoRender.h"
#include "CommonInclude.h"
#include "BaseGLRender.h"

#define MATH_PI 3.1415926535897932384626433832802
#define TEXTURE_NUM 3

using namespace glm;

class VideoGLRender : public BaseGLRender, public VideoRender {
public:
    virtual void Init(int videoWidth, int videoHeight, int *dstSize);

    virtual void RenderVideoFrame(NativeImage *pImage);

    virtual void UnInit();

    virtual void OnSurfaceCreated();

    virtual void OnSurfaceChanged(int w, int h);

    virtual void OnDrawFrame();

    static VideoGLRender *GetInstance();

    static void ReleaseInstance();

    virtual void UpdateMVPMatrix(int angleX, int angleY, float scaleX, float scaleY);

    virtual void UpdateMVPMatrix(TransformMatrix *pTransformMatrix);

    virtual void SetTouchLoc(float touchX, float touchY) {
        m_TouchXY.x = touchX / m_ScreenSize.x;
        m_TouchXY.y = touchY / m_ScreenSize.y;
    }

private:
    VideoGLRender();

    virtual ~VideoGLRender();

    static std::mutex m_Mutex;
    static VideoGLRender *s_Instance;
    GLuint m_ProgramObj = GL_NONE;
    GLuint m_TextureIds[TEXTURE_NUM]{};
    GLuint m_VaoId{};
    GLuint m_VboIds[3]{};
    NativeImage m_RenderImage;
    glm::mat4 m_MVPMatrix;

    int m_FrameIndex{};
    vec2 m_TouchXY;
    vec2 m_ScreenSize;
};


#endif //ANDROIDEXAMPLE_VIDEOGLRENDER_H
