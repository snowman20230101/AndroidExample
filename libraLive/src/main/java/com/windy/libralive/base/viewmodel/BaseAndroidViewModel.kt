package com.windy.libralive.base.viewmodel

import android.app.Application
import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.AndroidViewModel

abstract class BaseAndroidViewModel constructor(@NonNull application: Application) :
    AndroidViewModel(application) {

    protected open var modelClassName: String = BaseAndroidViewModel::class.java.simpleName

    init {
        Log.d("BaseAndroidViewModel", "init: $modelClassName")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("BaseAndroidViewModel", "onCleared: $modelClassName")
    }
}