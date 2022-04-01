package com.windy.libralive.ui.me

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.windy.libralive.base.BaseViewModel

class MeViewModel : BaseViewModel() {

    val text: LiveData<String> = MutableLiveData<String>().apply {
        value = "This is me Fragment"
    }
}