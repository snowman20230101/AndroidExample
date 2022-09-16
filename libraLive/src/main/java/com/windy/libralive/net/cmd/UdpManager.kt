package com.windy.libralive.net.cmd

import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket

enum class CmdType(name: String) {
    PLAY("play"),
    STOP("stop")
}

class UdpManager {
    private var socket: DatagramSocket? = null
    private var packet: DatagramPacket? = null
    private var port: Int = 8001

    internal var listener: CmdListener? = null

    @Volatile
    private var flag: Boolean = false

    fun init(type: Int) {

    }

    fun playServer() {
        socket = DatagramSocket(port)
        val buf = ByteArray(1024 * 64)
        packet = DatagramPacket(buf, buf.size)
        flag = true

        // 开始接受数据
        receive()
    }

    private fun receive() {
        Thread {
            while (flag) {
                try {
                    socket?.receive(packet) // 阻塞
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                synchronized(UdpManager::class.java) {
                    packet?.let {
                        val dataStr = String(it.data, 0, it.length)
                        log("收到了ip为：" + it.address + " 端口号为：" + it.port + "的消息：" + dataStr)

                        // 分发消息
                        dispatchMessage(dataStr)
                    }
                }
            }
        }.start()
    }

    private fun dispatchMessage(msg: String) {
        // 1. 解析数据
        val obj = JSONObject(msg)
        // 2. 按类型分发响应的指令
        when (obj.opt("cmd")) {
            CmdType.PLAY.name -> {
                log("播放 。。。。")
                listener?.onPlay(obj.optString("url"))
            }
            CmdType.STOP.name -> {
                log("暂停 。。。。")
                listener?.onStop()
            }
            else -> {
                log("为扩展的消息: $msg")
            }
        }
    }

    fun stopServer() {
        flag = false
        socket?.close()
    }

    fun sendMsg(): String {
        val obj = JSONObject()
        obj.put("cmd", "play")
        obj.put("url", "")
        return obj.toString()
    }

    fun log(msg: String) {
        Log.d("UdpManager", "log: $msg")
    }
}