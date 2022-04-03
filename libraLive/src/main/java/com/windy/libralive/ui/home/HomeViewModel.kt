package com.windy.libralive.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.windy.libralive.base.BaseViewModel

class HomeViewModel : BaseViewModel() {

    val text: LiveData<String> = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    val state = MutableLiveData<Int>().apply {
        this.value = 0
    }

    init {
        modelClassName = HomeViewModel::class.java.simpleName
    }
}