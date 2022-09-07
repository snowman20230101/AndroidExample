//
// Created by windy on 2022/8/1.
//

#ifndef ANDROIDEXAMPLE_EGLCORE_H
#define ANDROIDEXAMPLE_EGLCORE_H

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <EGL/eglplatform.h>

#include <CommonInclude.h>

/**
 * Constructor flag: surface must be recordable.  This discourages EGL from using a
 * pixel format that cannot be converted efficiently to something usable by the video
 * encoder.
 */
#define FLAG_RECORDABLE 0x01

/**
 * Constructor flag: ask for GLES3, fall back to GLES2 if not available.  Without this
 * flag, GLES2 is used.
 */
#define FLAG_TRY_GLES3 002

/**
 * Android-specific extension
 */
#define EGL_RECORDABLE_ANDROID 0x3142

// 时间
typedef EGLBoolean (EGLAPIENTRYP EGL_PRESENTATION_TIME_ANDROIDPROC)(EGLDisplay display,
                                                                    EGLSurface surface,
                                                                    khronos_stime_nanoseconds_t time);

class EglCore {
public:
    EglCore();

    ~EglCore();

    EglCore(EGLContext sharedContext, int flags);

    bool init(EGLContext sharedContext, int flags);

    // 获取EglContext
    EGLContext getEGLContext();

    // 销毁 Surface
    void releaseSurface(EGLSurface eglSurface);

    //  创建EGLSurface
    EGLSurface createWindowSurface(ANativeWindow *surface);

    // 创建离屏EGLSurface
    EGLSurface createOffscreenSurface(int width, int height);

    // 切换到当前上下文
    void makeCurrent(EGLSurface eglSurface);

    // 切换到某个上下文
    void makeCurrent(EGLSurface drawSurface, EGLSurface readSurface);

    // 没有上下文
    void makeNothingCurrent();

    // 交换显示
    bool swapBuffers(EGLSurface eglSurface);

    // 设置 pts
    void setPresentationTime(EGLSurface eglSurface, long nsecs);

    // 判断是否属于当前上下文
    bool isCurrent(EGLSurface eglSurface);

    // 执行查询
    int querySurface(EGLSurface eglSurface, int what);

    // 查询字符串
    const char *queryString(int what);

    // 获取当前的GLES 版本号
    int getGlVersion() const;

    // 检查是否出错
    static void checkEglError(const char *msg);

    // 释放资源
    void release();

private:
    EGLDisplay mEGLDisplay = EGL_NO_DISPLAY;
    EGLContext mEGLContext = EGL_NO_CONTEXT;
    EGLConfig mEGLConfig = NULL;

    int mGlVersion = -1;

    // 查找合适的EGLConfig
    EGLConfig getConfig(int flags, int version);

    // 设置时间戳方法
    EGL_PRESENTATION_TIME_ANDROIDPROC eglPresentationTimeANDROID = NULL;
};


#endif //ANDROIDEXAMPLE_EGLCORE_H
