package com.windy.libralive.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.windy.libralive.base.BaseViewModel

class HomeViewModel : BaseViewModel() {

    val text: LiveData<String> = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    init {
        modelClassName = HomeViewModel::class.java.simpleName
    }
}