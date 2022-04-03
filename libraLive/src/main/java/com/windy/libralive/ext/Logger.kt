package com.windy.libralive.ext

import android.util.Log

private const val debug: Boolean = false
private const val TAG = "Logger:"

fun d(msg: String) {
    d(TAG, msg)
}

fun i(msg: String) {
    i(TAG, msg)
}

fun e(msg: String) {
    e(TAG, msg)
}

fun w(msg: String) {
    w(TAG, msg)
}

fun d(tag: String, msg: String) {
    if (debug)
        Log.d(tag, msg)
}

fun i(tag: String, msg: String) {
    if (debug)
        Log.i(tag, msg)
}

fun e(tag: String, msg: String) {
    if (debug)
        Log.e(tag, msg)
}

fun w(tag: String, msg: String) {
    if (debug)
        Log.w(tag, msg)
}
