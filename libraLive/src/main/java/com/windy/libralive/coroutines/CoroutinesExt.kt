package com.windy.libralive.coroutines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * crossinline 避免非局部返回。
 */
inline fun <T> ViewModel.fetch(
    noinline block: suspend CoroutineScope.() -> Result<T>,
    crossinline onSuccess: (T) -> Unit,
    crossinline onFailure: (Throwable) -> Unit
) =
    viewModelScope.launch {
        block.invoke(this).run {
            onSuccess { onSuccess(it) }
            onFailure { onFailure(it) }
        }
    }.invokeOnCompletion {
        println(it?.message)
    }
