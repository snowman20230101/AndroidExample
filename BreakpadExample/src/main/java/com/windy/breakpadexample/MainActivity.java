package com.windy.breakpadexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btnAnr;
    private Button btnNativeCrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Log.i(TAG, "onCreate: " + getStringFromJni());
        btnAnr = findViewById(R.id.btn_anr);
        btnAnr.setOnClickListener(this);

        btnNativeCrash = findViewById(R.id.btn_native_crash);
        btnNativeCrash.setOnClickListener(this);

        findViewById(R.id.btn_gameView).setOnClickListener(this);

        initBreakpad();
    }

    private void initBreakpad() {
        // 开启ndk监控
        File file = new File(getExternalCacheDir(), "native_crash");
        if (!file.exists()) {
            file.mkdir();
        }
        initBreakpad(file.getAbsolutePath());
    }

    public native String getStringFromJni();

    public native void initBreakpad(String path);

    public native void testNativeCrash();


    static {
        System.loadLibrary("BreakExample");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_anr:
                startActivity(new Intent(MainActivity.this, AnrActivity.class));
                break;
            case R.id.btn_native_crash:
                testNativeCrash();
                break;

            case R.id.btn_gameView:
                startActivity(new Intent(MainActivity.this, GameActivity.class));
                break;
            default:
                break;
        }
    }
}