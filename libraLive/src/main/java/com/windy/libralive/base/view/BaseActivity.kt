package com.windy.libralive.base.view

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import org.jetbrains.annotations.NotNull

abstract class BaseActivity : AppCompatActivity() {
    protected open val TAG: String
        get() = BaseActivity::class.java.simpleName

    private lateinit var viewModelProvider: ViewModelProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Build.VERSION.SDK_INT = ${Build.VERSION.SDK_INT}")
        checkPermission()
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {

            }
        })

        hookViewModeStoreMap()
    }

    private fun hookViewModeStoreMap() {
        val storeClass = ViewModelStore::class.java
        val declaredField = storeClass.getDeclaredField("mMap")
        declaredField.isAccessible = true
        val mapObj = declaredField.get(viewModelStore)

        val mapClass = mapObj::class.java
        val sizeMethod = mapClass.getDeclaredMethod("size")
        val size = sizeMethod.invoke(mapObj)
        Log.d(TAG, "hookViewModeStoreMap: viewModel map size is $size")

        val entrySetMethod = mapClass.getDeclaredMethod("keySet") // Set<K>
        val keySetValues = entrySetMethod.invoke(mapObj)

        Log.d(TAG, "hookViewModeStoreMap: valuesObj=$keySetValues")
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
    ): T = getViewModelProvider()[modelClass]

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

    protected fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= 19) {
            Log.d(TAG, "hideSystemUI: ")
//            this.binding.surfaceView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        or View.SYSTEM_UI_FLAG_FULLSCREEN
//                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//            )

            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            );
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            );
        }
    }

    protected open fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions: MutableList<String> = ArrayList()
            // 写
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            // 相机
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                permissions.add(Manifest.permission.CAMERA)
            }
            // 录音
//            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.RECORD_AUDIO
//                )
//            ) {
//                permissions.add(Manifest.permission.RECORD_AUDIO)
//            }
            // 读
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            // 悬浮
//            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.SYSTEM_ALERT_WINDOW
//                )
//            ) {
//                permissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
//            }
//            // 悬浮
//            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.HIDE_OVERLAY_WINDOWS
//                )
//            ) {
//                permissions.add(Manifest.permission.HIDE_OVERLAY_WINDOWS)
//            }
            if (permissions.size != 0) {
                ActivityCompat.requestPermissions(
                    this,
                    permissions.toTypedArray(),
                    REQ_PERMISSION_CODE
                )
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_PERMISSION_CODE -> {
                for (ret in grantResults) {
                    if (PackageManager.PERMISSION_GRANTED == ret) {
                        grantedCount++
                    }
                }

                Log.e(
                    TAG,
                    "onRequestPermissionsResult: grantedCount=$grantedCount , ${permissions.size}"
                )

                if (grantedCount == permissions.size) {
                    onPermissionGranted()
                } else {
                    Toast.makeText(
                        this,
                        "用户没有允许需要的权限，加入通话失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                grantedCount = 0
            }
            else -> {}
        }
    }


    companion object {
        protected const val REQ_PERMISSION_CODE = 0x1000
    }

    protected var grantedCount = 0

    protected open fun onPermissionGranted() {

    }

}


