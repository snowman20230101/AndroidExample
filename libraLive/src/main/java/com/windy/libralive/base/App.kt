package com.windy.libralive.base

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.windy.libralive.external.NativeCaseTesting
import java.io.File

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // 开启ndk监控
        val file = File(externalCacheDir, "native_crash")
        if (!file.exists()) {
            file.mkdir()
        }

        val fileLog = File(getExternalFilesDir(""), "okay_log")
        if (!fileLog.exists())
            fileLog.mkdir()

        NativeCaseTesting.initBreakPad(file.absolutePath, fileLog.absolutePath)
        NativeCaseTesting.stringFromJNI()
    }

    companion object {
        // Used to load the 'libarlive' library on application startup.
        init {
            System.loadLibrary("libralive")
        }
    }

}