package com.windy.libralive.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CameraPreView extends GLSurfaceView {
    public CameraPreView(Context context) {
        super(context, null);
    }

    public CameraPreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 配置GLSurfaceView
        // 设置EGL版本
        setEGLContextClientVersion(2);
        setRenderer(new CameraRender(this));
        // 设置按需渲染 当我们调用 requestRender 请求GLThread 回调一次 onDrawFrame
        // 连续渲染 就是自动的回调onDrawFrame
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
