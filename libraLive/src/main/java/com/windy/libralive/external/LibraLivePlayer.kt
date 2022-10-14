package com.windy.libralive.external

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView

class LibraLivePlayer(private val dataSource: String) : SurfaceHolder.Callback {

    lateinit var holder: SurfaceHolder
    lateinit var listener: OnPrepareListener
    var playerHandle: Long = 0

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        native_setSurface(holder.surface)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    fun setSurfaceView(surfaceView: SurfaceView) {
        holder = surfaceView.holder
        holder.addCallback(this)
    }

    fun onPrepare() {
        if (listener != null) {
            listener.onPrepare()
        }
    }

    /**
     * JNI DETECTED ERROR IN APPLICATION: JNI GetMethodID called with pending exception java.lang.NoSuchMethodError:
     * no non-static method "Lcom/windy/libarlive/LibarLivePlayer;.onError(I)V"
     *
     *
     *  --------- beginning of crash
     *  2022-03-17 19:31:59.806 29935-30077/com.windy.libarlive A/libc: invalid pthread_t 0x2e006d passed to pthread_join
     *  2022-03-17 19:31:59.806 29935-30077/com.windy.libarlive A/libc: Fatal signal 6 (SIGABRT), code -1 (SI_QUEUE) in tid 30077 (windy.libarlive), pid 29935 (windy.libarlive)
     *
     *
     */

    fun onError(errCode: Int) = println("Java接到回调: $errCode")

    fun setOnPrepareListener(listener: OnPrepareListener) {
        this.listener = listener
    }

    fun prepare() {
        playerHandle = native_prepare(dataSource)
    }

    fun start() = native_start(playerHandle)

    fun stop() = native_stop(playerHandle)

    fun release() {
        holder.removeCallback(this)
        native_release(playerHandle)
        playerHandle = 0
    }

    private external fun native_prepare(dataSource: String): Long

    private external fun native_start(playerHandle: Long)

    private external fun native_stop(playerHandle: Long)

    private external fun native_release(playerHandle: Long)

    private external fun native_setSurface(surface: Surface)

    interface OnPrepareListener {
        fun onPrepare()
    }
}