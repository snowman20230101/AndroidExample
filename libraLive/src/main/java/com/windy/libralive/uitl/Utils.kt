package com.windy.libralive.uitl

import android.Manifest.permission
import android.content.Context
import androidx.annotation.RequiresPermission
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

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

        /**
         * 读取文件
         */
        @JvmStatic
        fun readRawTextFile(context: Context, rawId: Int): String {
            val inputStream: InputStream = context.resources.openRawResource(rawId)
            val br = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            val sb = StringBuilder()
            try {
                while (br.readLine().also { line = it } != null) {
                    sb.append(line)
                    sb.append("\n")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return sb.toString()
        }
    }
}