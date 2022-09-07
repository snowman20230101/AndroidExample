//
// Created by windy on 2022/8/1.
//

#include "EglSurfaceBase.h"

EglSurfaceBase::EglSurfaceBase(EglCore *eglCore) : mEglCore(eglCore) {
    mEglSurface = EGL_NO_SURFACE;
}

void EglSurfaceBase::createWindowSurface(ANativeWindow *nativeWindow) {
    assert(nativeWindow == EGL_NO_SURFACE);
    if (mEglSurface != EGL_NO_SURFACE) {
        LOGE("surface already created\n");
        return;
    }

    mEglCore->createWindowSurface(nativeWindow);
}

void EglSurfaceBase::createOffscreenSurface(int width, int height) {
    assert(mEglSurface == EGL_NO_SURFACE);
    if (mEglSurface != EGL_NO_SURFACE) {
        LOGE("surface already created\n");
        return;
    }

    mEglSurface = mEglCore->createOffscreenSurface(width, height);
}

int EglSurfaceBase::getWidth() {
    if (mWidth < 0) {
        return mEglCore->querySurface(mEglSurface, EGL_WIDTH);
    } else {
        return mWidth;
    }
}

int EglSurfaceBase::getHeight() {
    if (mHeight < 0) {
        return mEglCore->querySurface(mEglSurface, EGL_HEIGHT);
    } else {
        return mHeight;
    }
}

void EglSurfaceBase::releaseEglSurface() {
    mEglCore->releaseSurface(mEglSurface);
    mEglSurface = EGL_NO_SURFACE;
    mWidth = mHeight = -1;
}

void EglSurfaceBase::makeCurrent() {
    mEglCore->makeCurrent(mEglSurface);
}

bool EglSurfaceBase::swapBuffers() {
    bool result = mEglCore->swapBuffers(mEglSurface);
    if (!result) {
        LOGE("WARNING: swapBuffers() failed");
    }

    return result;
}

void EglSurfaceBase::setPresentationTime(long nsecs) {
    mEglCore->setPresentationTime(mEglSurface, nsecs);
}

// __attribute__((unused))
char *EglSurfaceBase::getCurrentFrame() {
    char *pixels = nullptr;
    glReadPixels(0, 0, getWidth(), getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, pixels);
    return pixels;
}
