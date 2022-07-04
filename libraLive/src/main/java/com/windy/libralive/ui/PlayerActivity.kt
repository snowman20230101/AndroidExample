package com.windy.libralive.ui

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.windy.libralive.R

import com.windy.libralive.base.view.BaseActivity
import com.windy.libralive.databinding.ActivityPlayerBinding
import com.windy.libralive.external.LibraLivePlayer

class PlayerActivity : BaseActivity() {

    private lateinit var binding: ActivityPlayerBinding
    lateinit var player: LibraLivePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        supportActionBar?.hide()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_player)
        hideSystemUI()

        val stringExtra = intent.getStringExtra("url")
        Log.d(TAG, "onCreate: $stringExtra")

        player = LibraLivePlayer(stringExtra!!)
        player.setSurfaceView(binding.surfaceView)
        player.setOnPrepareListener(object : LibraLivePlayer.OnPrepareListener {
            override fun onPrepare() {
                runOnUiThread {
                    Toast.makeText(this@PlayerActivity, "开始播放", Toast.LENGTH_SHORT).show()
                }

                player.start()
            }
        })
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
}