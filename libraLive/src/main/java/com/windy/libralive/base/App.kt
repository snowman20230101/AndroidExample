package com.windy.libralive.base

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.windy.libralive.external.NativeCaseTesting

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        NativeCaseTesting.stringFromJNI()
    }

    companion object {
        // Used to load the 'libarlive' library on application startup.
        init {
            System.loadLibrary("libralive")
        }
    }

}