package com.windy.libralive.opengl;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.windy.libralive.R;
import com.windy.libralive.uitl.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ScreenFilter {
    private final FloatBuffer vertFloatBuffer;
    private final FloatBuffer textureFlatBuffer;

    private int width, height;
    private final int programId;

    private final int vPosition;
    private final int vCoord;
    private final int vMatrix;

    private final int vTexture;

    public ScreenFilter(Context context) {
        String vertSource = Utils.readRawTextFile(context, R.raw.vert_flag);
        String fragSource = Utils.readRawTextFile(context, R.raw.frag_flag);

        Log.e("ScreenFilter：", "vertSource=" + vertSource);

        // 顶点着色器
        int vertShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShaderId, vertSource);
        GLES20.glCompileShader(vertShaderId);

        // 主动获取成功、失败 (如果不主动查询，只输出 一条 GLERROR之类的日志，很难定位到到底是那里出错)
        int[] status = new int[1];
        GLES20.glGetShaderiv(vertShaderId, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String infoLog = GLES20.glGetShaderInfoLog(vertShaderId);
            GLES20.glDeleteShader(vertShaderId);
            throw new IllegalArgumentException("ScreenFilter 顶点着色器配置失败!" + infoLog);
        }

        // 片元着色器
        int fragShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShaderId, fragSource);
        GLES20.glCompileShader(fragShaderId);

        GLES20.glGetShaderiv(fragShaderId, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String infoLog = GLES20.glGetShaderInfoLog(fragShaderId);
            GLES20.glDeleteShader(fragShaderId);
            throw new IllegalArgumentException("ScreenFilter 片元着色器配置失败!" + infoLog);
        }

        // 创建着色器程序
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertShaderId);
        GLES20.glAttachShader(program, fragShaderId);
        GLES20.glLinkProgram(program);

        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String infoLog = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            throw new IllegalArgumentException("ScreenFilter 着色器程序配置失败!" + infoLog);
        }

        // 删除着色器
        GLES20.glDeleteShader(vertShaderId);
        GLES20.glDeleteShader(fragShaderId);

        programId = program;

        // 顶点
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        vCoord = GLES20.glGetAttribLocation(program, "vCoord");
        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix");

        // 片元
        vTexture = GLES20.glGetUniformLocation(program, "vTexture");

        // 顶点缓冲区
        vertFloatBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertFloatBuffer.clear();

        float[] v = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f
        };

        vertFloatBuffer.put(v);

        // 纹理缓冲区
        textureFlatBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureFlatBuffer.clear();

        //        float[] t = {0.0f, 1.0f,
//                1.0f, 1.0f,
//                0.0f, 0.0f,
//                1.0f, 0.0f};
        //旋转
//        float[] t = {1.0f, 1.0f,
//                1.0f, 0.0f,
//                0.0f, 1.0f,
//                0.0f, 0.0f};
        // 镜像
        float[] t = {
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                0.0f, 1.0f
        };
        textureFlatBuffer.put(t);
    }

    public void onDrawFrame(int textureId, float[] mtx) {
        // 1、设置窗口大小
        // 画画的时候 你的画布可以看成 10x10，也可以看成5x5 等等
        // 设置画布的大小，然后画画的时候， 画布越大，你画上去的图像就会显得越小
        // x与y 就是从画布的哪个位置开始画
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(programId);

        // 怎么画？ 其实就是传值
        // 2：xy 两个数据 float的类型
        // 1、将顶点数据传入，确定形状
        vertFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertFloatBuffer);
        // 传了数据之后 激活
        GLES20.glEnableVertexAttribArray(vPosition);

        // 2、将纹理坐标传入，采样坐标
        textureFlatBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureFlatBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        // 变化矩阵
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0);
        // 片元 vTexture 绑定图像数据到采样器
        // 激活图层
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // 图像数据
        // 正常：GLES20.GL_TEXTURE_2D
        // surfaceTexture 的纹理需要
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        // 传递参数 0：需要和纹理层GL_TEXTURE0对应
        GLES20.glUniform1i(vTexture, 0);
        // 参数传完了 通知opengl 画画 从第0点开始 共4个点
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void onReady(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
