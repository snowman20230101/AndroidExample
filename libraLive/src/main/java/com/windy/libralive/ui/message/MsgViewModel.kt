package com.windy.libralive.ui.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.windy.libralive.base.BaseViewModel

class MsgViewModel : BaseViewModel() {

    val text: LiveData<String> = MutableLiveData<String>().apply {
        value = "This is message Fragment"
    }

    val state = MutableLiveData<Int>().apply {
        value = 0
    }

    init {
        modelClassName = MsgViewModel::class.java.simpleName
    }
}