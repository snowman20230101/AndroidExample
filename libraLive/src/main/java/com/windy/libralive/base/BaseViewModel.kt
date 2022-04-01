package com.windy.libralive.base

import android.util.Log
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {

    protected var modelClassName: String = BaseViewModel::class.java.simpleName

    override fun onCleared() {
        super.onCleared()
        Log.d("BaseViewModel", "onCleared: $modelClassName")
    }
}
