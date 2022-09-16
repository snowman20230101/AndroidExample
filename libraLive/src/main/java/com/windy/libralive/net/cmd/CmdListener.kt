package com.windy.libralive.net.cmd

interface CmdListener {
    fun onPlay(url: String?) {

    }

    fun onStop() {

    }
}