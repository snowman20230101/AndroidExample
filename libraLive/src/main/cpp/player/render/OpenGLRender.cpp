//
// Created by windy on 2022/7/29.
//

#include "OpenGLRender.h"

OpenGLRender *OpenGLRender::s_Instance = nullptr;
//std::mutex OpenGLRender::m_Mutex;

/**
 * 顶点着色器
 */
static char vShaderStr[] =
        "#version 300 es\n"
        "layout(location = 0) in vec4 a_position;\n"
        "layout(location = 1) in vec2 a_texCoord;\n"
        "uniform mat4 u_MVPMatrix;\n"
        "out vec2 v_texCoord;\n"
        "void main()\n"
        "{\n"
        "    gl_Position = u_MVPMatrix * a_position;\n"
        "    v_texCoord = a_texCoord;\n"
        "}";

/**
 * 片元着色器
 */
static char fShaderStr[] =
        "#version 300 es\n"
        "precision highp float;\n"
        "in vec2 v_texCoord;\n"
        "layout(location = 0) out vec4 outColor;\n"
        "uniform sampler2D s_TextureMap;//采样器\n"
        "void main()\n"
        "{\n"
        "    outColor = texture(s_TextureMap, v_texCoord);\n"
        "}";

//GLfloat verticesCoords[] = {
//        -1.0f, 1.0f, 0.0f,  // Position 0
//        -1.0f, -1.0f, 0.0f,  // Position 1
//        1.0f, -1.0f, 0.0f,  // Position 2
//        1.0f, 1.0f, 0.0f,  // Position 3
//};
//
//GLfloat textureCoords[] = {
//        0.0f, 0.0f,        // TexCoord 0
//        0.0f, 1.0f,        // TexCoord 1
//        1.0f, 1.0f,        // TexCoord 2
//        1.0f, 0.0f         // TexCoord 3
//};
//
//GLushort indices[] = {0, 1, 2, 0, 2, 3};

OpenGLRender::OpenGLRender() {
    LOGD("OpenGLRender::OpenGLRender()");
}

OpenGLRender::~OpenGLRender() {
    LOGD("OpenGLRender::~OpenGLRender()");
}

void OpenGLRender::Init(int videoWidth, int videoHeight, int *dstSize) {

}

void OpenGLRender::UnInit() {

}

void OpenGLRender::OnSurfaceCreated() {

}

void OpenGLRender::OnSurfaceChanged(int w, int h) {

}

void OpenGLRender::OnDrawFrame() {

}

OpenGLRender *OpenGLRender::GetInstance() {
    return nullptr;
}

void OpenGLRender::ReleaseInstance() {

}

void OpenGLRender::UpdateMVPMatrix(int angleX, int angleY, float scaleX, float scaleY) {

}

void OpenGLRender::RenderVideoFrame(NativeImage *pImage) {

}
