package com.windy.libralive.base.view

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import org.jetbrains.annotations.NotNull

abstract class BaseActivity : AppCompatActivity() {
    var TAG = BaseActivity::class.java.simpleName

    private lateinit var viewModelProvider: ViewModelProvider;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//                TODO("Not yet implemented")
            }
        })
    }

    /**
     * ViewModel 工厂 AndroidViewModelFactory
     */
    protected fun getAppFactory(): ViewModelProvider.Factory =
        ViewModelProvider.AndroidViewModelFactory.getInstance(checkApplication(this))

    /**
     * 实例化一个 ViewModel
     */
    protected fun <T : ViewModel> createViewModel(
        @NotNull modelClass: Class<T>
    ): T = getViewModelProvider().get(modelClass)

    /**
     * 检查
     */
    private fun checkApplication(activity: Activity): Application {
        return activity.application ?: throw IllegalAccessException(
            "Your activity/fragment is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call."
        )
    }

    /**
     * 提供 ViewModel 内容提供者
     */
    protected fun getViewModelProvider(): ViewModelProvider {
        if (viewModelProvider == null) {
            viewModelProvider = ViewModelProvider(this, getAppFactory())
        }
        return viewModelProvider
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}


