package com.windy.breakpadexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btnAnr;
    private Button btnNativeCrash;
    private Button btnHandler;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            System.out.println("测试：" + msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Log.i(TAG, "onCreate: " + getStringFromJni());
        btnAnr = findViewById(R.id.btn_anr);
        btnAnr.setOnClickListener(this);

        btnNativeCrash = findViewById(R.id.btn_native_crash);
        btnNativeCrash.setOnClickListener(this);

        btnHandler = findViewById(R.id.btn_handler);
        btnHandler.setOnClickListener(this);

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

            case R.id.btn_handler:
//                testHandler();
                getOKHttp();

            default:
                break;
        }
    }

    public void getOKHttp() {
        // https://api.github.com/users/bboywindy
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.build();

        Request request = new Request.Builder()
                .url("https://api.github.com/users/bboywindy")
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: " + response.body().string());
            }
        });

    }

    private void testHandler() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 10);
                    Message obtain = Message.obtain();
                    obtain.obj = "我是中国人";
                    boolean message = mHandler.sendMessageDelayed(obtain, 3000);
                    System.out.println("message :" + message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}