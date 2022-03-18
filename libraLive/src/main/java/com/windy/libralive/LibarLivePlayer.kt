package com.windy.libralive

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView

class LibarLivePlayer(val dataSource: String) : SurfaceHolder.Callback {

    lateinit var holder: SurfaceHolder
    lateinit var listener: OnPrepareListener

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
     * JNI DETECTED ERROR IN APPLICATION: JNI GetMethodID called with pending exception java.lang.NoSuchMethodError: no non-static method "Lcom/windy/libarlive/LibarLivePlayer;.onError(I)V"
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

    fun prepare() = native_prepare(dataSource)

    fun start() = native_start()

    fun stop() = native_stop()

    fun release() {
        holder.removeCallback(this)
        native_release()
    }

    private external fun native_prepare(dataSource: String)

    private external fun native_start()

    private external fun native_stop()

    private external fun native_release()

    private external fun native_setSurface(surface: Surface)

    interface OnPrepareListener {
        fun onPrepare()
    }
}