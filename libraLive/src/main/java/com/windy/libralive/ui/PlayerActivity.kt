package com.windy.libralive.ui

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast

import com.windy.libralive.base.BaseActivity
import com.windy.libralive.databinding.ActivityPlayerBinding
import com.windy.libralive.external.LibraLivePlayer

class PlayerActivity : BaseActivity() {

    private val TAG: String? = PlayerActivity::class.simpleName

    lateinit var binding: ActivityPlayerBinding
    lateinit var player: LibraLivePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        hideSystemUI()
        setContentView(binding.root)

        val stringExtra = intent.getStringExtra("url")
        Log.d(TAG, "onCreate: $stringExtra")

        player = LibraLivePlayer(stringExtra!!)
        player.setSurfaceView(binding.surfaceView)
        player.setOnPrepareListener(object : LibraLivePlayer.OnPrepareListener {
            override fun onPrepare() {
                runOnUiThread(object : Runnable {
                    override fun run() {
                        Toast.makeText(this@PlayerActivity, "开始播放", Toast.LENGTH_SHORT).show()
                    }
                })

                player.start()
            }
        })

        binding = ActivityPlayerBinding.inflate(layoutInflater);

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                    .LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        setContentView(binding.getRoot());
        player.setSurfaceView(binding.surfaceView)
    }

    override fun onResume() {
        super.onResume()
        player.prepare()
    }

    override fun onStop() {
        super.onStop()
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= 19) {
            Log.d(TAG, "hideSystemUI: ")
            this.binding.surfaceView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )

            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            );
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            );
        }
    }
}