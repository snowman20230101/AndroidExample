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

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends BaseActivity implements View.OnClickListener {

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
        TAG = MainActivity.class.getSimpleName();
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
        findViewById(R.id.btn_start_act).setOnClickListener(this);

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
                Intent in = new Intent(MainActivity.this, GameActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);
                break;

            case R.id.btn_handler:
//                testHandler();
                getOKHttp();
                break;
            case R.id.btn_start_act:
                break;

            default:
                break;
        }
    }

    public void getOKHttp() {
        // https://api.github.com/users/bboywindy
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(httpLoggingInterceptor);
        builder.cache(new Cache(new File(getExternalFilesDir(""), "okhttp_cache"), 10 * 1024 * 1024));
        OkHttpClient okHttpClient = builder.build();

//        RequestBody requestBody = new MultipartBody.Builder().build();
        FormBody formBody = new FormBody.Builder().build();


        Request request = new Request.Builder()
                .cacheControl(new CacheControl.Builder().build())
//                .post(formBody)
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