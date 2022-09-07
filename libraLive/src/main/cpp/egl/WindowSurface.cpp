//
// Created by windy on 2022/8/1.
//

#include <android/native_window_jni.h>
#include "WindowSurface.h"

WindowSurface::WindowSurface(EglCore *eglCore, ANativeWindow *window, bool releaseSurface)
        : EglSurfaceBase(eglCore) {
    mSurface = window;
    createWindowSurface(window);
    mReleaseSurface = releaseSurface;
}

WindowSurface::WindowSurface(EglCore *eglCore, ANativeWindow *window) : EglSurfaceBase(eglCore) {
    mSurface = window;
    createWindowSurface(window);
}

void WindowSurface::release() {
    releaseEglSurface();
    if (mSurface != nullptr) {
        ANativeWindow_release(mSurface);
        mSurface = nullptr;
    }
}

void WindowSurface::recreate(EglCore *eglCore) {
    assert(mSurface != NULL);
    if (mSurface == NULL) {
        LOGE("not yet implemented ANativeWindow");
        return;
    }
    mEglCore = eglCore;
    createWindowSurface(mSurface);
}
