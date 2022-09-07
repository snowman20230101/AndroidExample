package com.windy.libralive.ui

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.windy.libralive.R
import com.windy.libralive.base.view.BaseActivity
import com.windy.libralive.databinding.ActivitySecBinding
import com.windy.libralive.ext.applicationViewModels

class SecondActivity : BaseActivity() {

    override val TAG: String
        get() = SecondActivity::class.java.simpleName

    private lateinit var binding: ActivitySecBinding

    //    private lateinit var viewModel: SharedViewModel
    private val viewModel: SharedViewModel by applicationViewModels<SharedViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView<ActivitySecBinding>(this, R.layout.activity_sec)
//        viewModel = ViewModelProvider(this, getAppFactory())[SharedViewModel::class.java]
        binding.vm = viewModel

        viewModel.state.observe(this) {
            Log.d(TAG, "onCreate: $it")
        }

        binding.text2.setOnClickListener {
            viewModel.state.value = viewModel.state.value?.plus(1)
        }
    }
}