//
// Created by windy on 2022/7/29.
//

#ifndef ANDROIDEXAMPLE_OPENGLRENDER_H
#define ANDROIDEXAMPLE_OPENGLRENDER_H

#include "VideoRender.h"

// openGL 数学
//#include <glm.hpp>
//#include <gtc/matrix_transform.hpp>
//#include <gtc/type_ptr.hpp>

//#include <GLES3/gl3.h>

#include "CommonInclude.h"

// OpenGLRender 类定义
class OpenGLRender : public VideoRender {
public:
    virtual void Init(int videoWidth, int videoHeight, int *dstSize);

    virtual void RenderVideoFrame(NativeImage *pImage);

    virtual void UnInit();

    // 对应 Java 层 GLSurfaceView.Renderer 的三个接口
    void OnSurfaceCreated();

    void OnSurfaceChanged(int w, int h);

    void OnDrawFrame();

    //静态实例管理
    static OpenGLRender *GetInstance();

    static void ReleaseInstance();

    //设置变换矩阵，控制图像的旋转缩放
    void UpdateMVPMatrix(int angleX, int angleY, float scaleX, float scaleY);

private:
    OpenGLRender();

    virtual ~OpenGLRender();

//    static std::mutex m_Mutex;

    static OpenGLRender *s_Instance;

//    GLuint m_ProgramObj = GL_NONE;
//
//    GLuint m_TextureId { };
//
//    GLuint m_VaoId{};
//
//    GLuint m_VboIds[3] { };

    NativeImage m_RenderImage { };

//    glm::mat4 m_MVPMatrix; // 变换矩阵
};


#endif //ANDROIDEXAMPLE_OPENGLRENDER_H
