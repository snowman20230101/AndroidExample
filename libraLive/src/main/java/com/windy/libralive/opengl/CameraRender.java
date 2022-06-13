package com.windy.libralive.opengl;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    // 显示到SurfaceView
    private final GLSurfaceView surfaceView;
    // 纹理绘制
    private SurfaceTexture surfaceTexture;
    // 摄像头
    private CameraHelper helper;

    private int[] textures;
    // 矩阵
    private final float[] mtx = new float[16];

    private ScreenFilter filter;

    public CameraRender(GLSurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        helper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_FRONT);
        // 通过opengl创建一个纹理id
        textures = new int[1];
        GLES20.glGenTextures(textures.length, textures, 0);
        surfaceTexture = new SurfaceTexture(textures[0]);
        surfaceTexture.setOnFrameAvailableListener(this);

        filter = new ScreenFilter(surfaceView.getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        helper.startPreview(surfaceTexture);
        filter.onReady(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 把摄像头的数据先输出来
        // 更新纹理，然后我们才能够使用 opengl 从 SurfaceTexture 当中获得数据 进行渲染
        surfaceTexture.updateTexImage();
        // surfaceTexture 比较特殊，在opengl当中 使用的是特殊的采样器 samplerExternalOES （不是sampler2D）
        // 获得变换矩阵
        surfaceTexture.getTransformMatrix(mtx);

        filter.onDrawFrame(textures[0], mtx);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        surfaceView.requestRender();
    }
}
