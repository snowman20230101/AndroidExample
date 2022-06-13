package com.windy.libralive.ui

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.windy.libralive.R
import com.windy.libralive.base.view.BaseActivity
import com.windy.libralive.databinding.ActivityCameraBinding

class CameraActivity : BaseActivity() {
    lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView<ActivityCameraBinding>(this, R.layout.activity_camera)


        hideSystemUI()
        supportActionBar?.hide()
    }
}