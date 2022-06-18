package com.windy.libralive.ui

import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import androidx.databinding.DataBindingUtil
import com.windy.libralive.R
import com.windy.libralive.base.view.BaseActivity
import com.windy.libralive.databinding.ActivityMediaCodecBinding
import com.windy.libralive.external.H264DeCodePlay
import java.io.File

class MediaCodecActivity : BaseActivity() {
    lateinit var binding: ActivityMediaCodecBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView<ActivityMediaCodecBinding>(
                this,
                R.layout.activity_media_codec
            )
        hideSystemUI()
        supportActionBar?.hide()

        initView()
    }


   private fun initView() {
        binding.mediaCodecSurfaceV.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                val externalFilesDir = this@MediaCodecActivity.getExternalFilesDir("")
                val file = File(externalFilesDir, "douyinshipin.h264")
                val h264DeCodePlay = H264DeCodePlay(file.absolutePath, binding.mediaCodecSurfaceV.holder.surface)
                h264DeCodePlay.decodePlay()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.d(TAG, "surfaceChanged: ")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d(TAG, "surfaceDestroyed: ")
            }
        })
    }
}