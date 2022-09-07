package com.windy.libralive.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class RotationImageView extends androidx.appcompat.widget.AppCompatImageView {

    public RotationImageView(Context context) {
        super(context);
    }

    public RotationImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RotationImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() instanceof BitmapDrawable) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(false);
            Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            Matrix matrix = new Matrix();
            float scale;
            /* Note: this piece of code handling like Centre-Crop scaling */
            if (bitmap.getWidth() > bitmap.getHeight()) {
                scale = (float) canvas.getHeight() / (float) bitmap.getHeight();
                matrix.setScale(scale, scale);
                matrix.postTranslate((canvas.getWidth() - bitmap.getWidth() * scale) * 0.5f, 0);

            } else {
                scale = (float) canvas.getWidth() / (float) bitmap.getWidth();
                matrix.setScale(scale, scale);
                matrix.postTranslate(0, (canvas.getHeight() - bitmap.getHeight() * scale) * 0.5f);
            }
            shader.setLocalMatrix(matrix);

            paint.setShader(shader);
            /* this is where I shrink the image by 1px each side,
                move it to the center */
            canvas.translate(10, 10);
            canvas.drawRect(
                    0.0f, 0.0f, canvas.getWidth() - 4, canvas.getHeight() - 4, paint);

        } else {
            super.onDraw(canvas);
        }
    }
}