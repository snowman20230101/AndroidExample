package com.windy.libarlive

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.windy.libarlive.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private val TAG: String? = PlayerActivity::class.simpleName

    lateinit var binding: ActivityPlayerBinding
    lateinit var player: LibarLivePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        hideSystemUI()
        setContentView(binding.root)

        val stringExtra = intent.getStringExtra("url")
        Log.d(TAG, "onCreate: $stringExtra")

        player = LibarLivePlayer(stringExtra!!)
        player.setSurfaceView(binding.surfaceView)
        player.setOnPrepareListener(object : LibarLivePlayer.OnPrepareListener {
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
            this.binding.surfaceView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
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