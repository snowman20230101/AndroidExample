package com.windy.libralive.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.windy.libralive.R
import com.windy.libralive.base.view.BaseActivity
import com.windy.libralive.databinding.ActivityFirstBinding
import com.windy.libralive.ext.applicationViewModels

class FirstActivity : BaseActivity() {

    override val TAG: String
        get() = FirstActivity::class.java.simpleName

    private lateinit var binding: ActivityFirstBinding
    private lateinit var viewModel: SharedViewModel
    private val viewModel1: SharedViewModel by applicationViewModels<SharedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView<ActivityFirstBinding>(this, R.layout.activity_first)
        viewModel = ViewModelProvider(this, getAppFactory())[SharedViewModel::class.java]
        viewModel = createViewModel(SharedViewModel::class.java)
        binding.vm = viewModel

        viewModel.state.observe(this) {
            Log.d(TAG, "onCreate: $it")
        }

        binding.text1.setOnClickListener {
            viewModel.state.value = viewModel.state.value?.plus(1)
        }

        binding.btnJump.setOnClickListener {
            startActivity(Intent(this@FirstActivity, SecondActivity::class.java))
        }
    }
}