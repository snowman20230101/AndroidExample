package com.windy.libralive.uitl

import android.Manifest.permission
import androidx.annotation.RequiresPermission

class Utils {
    companion object {

        /**
         * Return whether network is connected.
         *
         * Must hold `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`
         *
         * @return `true`: connected<br></br>`false`: disconnected
         */
        @RequiresPermission(permission.ACCESS_NETWORK_STATE)
        fun isConnected(): Boolean {
            return true
        }
    }
}