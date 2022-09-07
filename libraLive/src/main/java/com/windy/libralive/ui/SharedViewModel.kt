package com.windy.libralive.ui

import android.app.Application
import com.windy.libralive.base.viewmodel.BaseAndroidViewModel
import com.windy.libralive.ext.SingleLiveEvent

class SharedViewModel(application: Application) : BaseAndroidViewModel(application) {
    val state = SingleLiveEvent<Int>().apply {
        value = 0
    }

    override var modelClassName: String
        get() = SharedViewModel::class.java.simpleName
        set(value) {

        }
}