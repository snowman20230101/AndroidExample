package com.windy.libralive.base.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    protected open var modelClassName: String = BaseViewModel::class.java.simpleName


    /**
     *
     */
    fun launchUI(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch { block() }

    override fun onCleared() {
        super.onCleared()
        Log.d("BaseViewModel", "onCleared: $modelClassName")
    }
}
