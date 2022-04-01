package com.windy.libralive.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import kotlin.reflect.KClass

class BaseLazy<T : ViewModel>(
    private val modelClass: KClass<T>,
    private val store: () -> ViewModelStore,
    private val factory: () -> ViewModelProvider.Factory
) : Lazy<T> {
    private var cache: T? = null
    override val value: T
        get() {
            val viewModel = cache
            val s = store()
            val f = factory()
            return viewModel ?: ViewModelProvider(s, f)[modelClass.java].also { cache = it }
        }

    override fun isInitialized(): Boolean = cache != null
}