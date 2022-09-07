//
// Created by windy on 2022/8/1.
//


#include "EglCore.h"

EglCore::EglCore() {
    init(nullptr, 0);
}

EglCore::~EglCore() {
    release();
}

EglCore::EglCore(EGLContext sharedContext, int flags) {
    init(sharedContext, flags);
}

/**
 * 初始化 egl
 * @param sharedContext
 * @param flags
 * @return
 */
bool EglCore::init(EGLContext sharedContext, int flags) {
    assert(mEGLDisplay == EGL_NO_DISPLAY);
    if (mEGLDisplay != EGL_NO_DISPLAY) {
        LOGE("EGL already set up");
    }

    if (sharedContext == nullptr) {
        sharedContext = EGL_NO_CONTEXT;
    }

    mEGLDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    assert(mEGLDisplay != EGL_NO_DISPLAY);
    if (mEGLDisplay == EGL_NO_DISPLAY) {
        LOGE("unable to get EGL14 display.\n");
    }

    if (!eglInitialize(mEGLDisplay, nullptr, nullptr)) {
        mEGLDisplay = EGL_NO_DISPLAY;
        LOGE("unable to initialize EGL14");
    }

    // GLES3
    if ((flags & FLAG_TRY_GLES3) != 0) {
        EGLConfig eglConfig = getConfig(flags, 3);
        if (eglConfig != nullptr) {
            int attrb3_list[] = {
                    EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL_NONE
            };

            EGLContext eglContext = eglCreateContext(mEGLDisplay, eglConfig, sharedContext,
                                                     attrb3_list);
            checkEglError("eglCreateContext");
            if (eglGetError() == EGL_SUCCESS) {
                mEGLConfig = eglConfig;
                mEGLContext = eglContext;
                mGlVersion = 3;
            }
        }
    }

    // GLES2
    if (mEGLContext == EGL_NO_CONTEXT) {
        EGLConfig eglConfig = getConfig(flags, 2);
        if (eglConfig != nullptr) {
            int attrib2_list[] = {
                    EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL_NONE
            };

            EGLContext eglContext = eglCreateContext(mEGLDisplay, eglConfig, sharedContext,
                                                     attrib2_list);
            checkEglError("eglCreateContext");
            if (eglGetError() == EGL_SUCCESS) {
                mEGLConfig = eglConfig;
                mEGLContext = eglContext;
                mGlVersion = 2;
            }
        }
    }

    eglPresentationTimeANDROID = (EGL_PRESENTATION_TIME_ANDROIDPROC) eglGetProcAddress(
            "eglPresentationTimeANDROID");

    if (!eglPresentationTimeANDROID) {
        LOGE("eglPresentationTimeANDROID is not available!");
    }

    EGLint values[1] = {0};
    EGLint attribute = EGL_CONTEXT_CLIENT_VERSION;
    eglQueryContext(mEGLDisplay, mEGLContext, attribute, values);
    LOGE("EGLContext created, client version is %d", values[0]);

    return true;
}

EGLContext EglCore::getEGLContext() {
    return mEGLContext;
}

void EglCore::releaseSurface(EGLSurface eglSurface) {
    eglDestroySurface(mEGLDisplay, eglSurface);
}

/**
 * 创建 EGLSurface
 *
 * @param surface
 * @return
 */
EGLSurface EglCore::createWindowSurface(ANativeWindow *surface) {
//    assert(surface != nullptr);
    if (surface == nullptr) {
        LOGE("ANativeWindow is NULL!");
        return nullptr;
    }

    EGLint attrib_list[] = {
            EGL_NONE
    };

    EGLSurface eglSurface = eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, attrib_list);
    checkEglError("eglCreateWindowSurface");
//    assert(eglSurface != nullptr);
    if (eglSurface == nullptr) {
        LOGE("EGLSurface is nullptr .");
    }

    return eglSurface;
}

/**
 * 创建离屏渲染的 EGLSurface
 *
 * @param width
 * @param height
 * @return
 */
EGLSurface EglCore::createOffscreenSurface(int width, int height) {
    EGLint attrib_list[] = {
            EGL_WIDTH, width,
            EGL_HEIGHT, height,
            EGL_NONE
    };

    EGLSurface eglSurface = eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, attrib_list);
    checkEglError("eglCreatePbufferSurface");
    if (eglSurface == nullptr) {
        LOGE("eglCreatePbufferSurface failed . eglSurface is NULL");
    }

    return eglSurface;
}

void EglCore::makeCurrent(EGLSurface eglSurface) {
    if (mEGLDisplay == EGL_NO_DISPLAY) {
        LOGE("Note: makeCurrent w/o display.\n");
    }
    if (!eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)) {
        // TODO 抛出异常
        LOGE("eglMakeCurrent failed . ");
    }
}

void EglCore::makeCurrent(EGLSurface drawSurface, EGLSurface readSurface) {
    if (mEGLDisplay == EGL_NO_DISPLAY) {
        LOGE("Note: makeCurrent w/o display.\n");
    }
    if (!eglMakeCurrent(mEGLDisplay, drawSurface, readSurface, mEGLContext)) {
        // TODO 抛出异常
        LOGE("eglMakeCurrent failed . ");
    }
}

void EglCore::makeNothingCurrent() {
    if (!eglMakeCurrent(mEGLDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)) {
        // TODO 抛出异常
        LOGE("eglMakeCurrent failed . ");
    }
}

bool EglCore::swapBuffers(EGLSurface eglSurface) {
    return eglSwapBuffers(mEGLDisplay, eglSurface);
}

void EglCore::setPresentationTime(EGLSurface eglSurface, long nsecs) {
    eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs);
}

bool EglCore::isCurrent(EGLSurface eglSurface) {
    return mEGLContext == eglGetCurrentContext() && eglSurface == eglGetCurrentSurface(EGL_DRAW);
}

int EglCore::querySurface(EGLSurface eglSurface, int what) {
    EGLint values[1] = {0};
    eglQuerySurface(mEGLDisplay, eglSurface, what, values);
    return values[0];
}

const char *EglCore::queryString(int what) {
    return eglQueryString(mEGLDisplay, what);
}

int EglCore::getGlVersion() const {
    return mGlVersion;
}

void EglCore::checkEglError(const char *msg) {
    int errorCode;
    errorCode = eglGetError() != EGL_SUCCESS;
    if (errorCode) {
        LOGE("%s EGL Error: %d", msg, errorCode);
    }
}

void EglCore::release() {
    LOGD("EglCore::release()");
    if (mEGLDisplay != EGL_NO_DISPLAY) {
        eglMakeCurrent(mEGLDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(mEGLDisplay, mEGLContext);
        eglReleaseThread();
        eglTerminate(mEGLDisplay);
    }

    mEGLDisplay = EGL_NO_DISPLAY;
    mEGLContext = EGL_NO_CONTEXT;
    mEGLConfig = nullptr;
}

EGLConfig EglCore::getConfig(int flags, int version) {
    int renderableType = EGL_OPENGL_ES2_BIT;
    if (version >= 3) {
        renderableType |= EGL_OPENGL_ES3_BIT_KHR;
    }

    // attribute
    int attribList[] = {
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_DEPTH_SIZE, 16,
            EGL_STENCIL_SIZE, 8,
            EGL_RENDERABLE_TYPE, renderableType,
            EGL_NONE, 0, // placeholder for recordable [@-3]
            EGL_NONE
    };

    int length = sizeof(attribList) / sizeof(attribList[0]);

    // ???
    if ((flags & FLAG_RECORDABLE) != 0) {
        attribList[length - 3] = EGL_RECORDABLE_ANDROID;
        attribList[length - 2] = 1;
    }

    EGLConfig configs = nullptr;
    int numConfigs;

    if (!eglChooseConfig(mEGLDisplay, attribList, &configs, 1, &numConfigs)) {
        LOGE("unable to find RGB8888 / %d  EGLConfig", version);
        return nullptr;
    }

    return configs;
}
