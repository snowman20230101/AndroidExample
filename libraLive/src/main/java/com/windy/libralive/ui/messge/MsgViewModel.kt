package com.windy.libralive.ui.messge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.windy.libralive.base.BaseViewModel

class MsgViewModel : BaseViewModel() {

    val text: LiveData<String> = MutableLiveData<String>().apply {
        value = "This is message Fragment"
    }
}