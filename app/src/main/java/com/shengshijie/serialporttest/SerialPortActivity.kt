package com.shengshijie.serialporttest

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.shengshijie.serialport.SerialPort
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.experimental.and

class SerialPortActivity : Activity() {
    private val mSerialPort = SerialPort()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.console)
        mSerialPort.init(
            File("dev/ttyS4"), 115200, 0
        ) { isSuccess, errorMsg ->
            if (isSuccess) {
                Log.e("CCC", "isSuccess")
            } else {
                Log.e("CCC", errorMsg)
            }
        }
    }

    private fun merge(hand: ByteArray?, tail: ByteArray): ByteArray {
        if (hand == null) {
            return tail
        }
        val data3 = ByteArray(hand.size + tail.size)
        System.arraycopy(hand, 0, data3, 0, hand.size)
        System.arraycopy(tail, 0, data3, hand.size, tail.size)
        return data3
    }

    @Throws(IOException::class)
    private fun receiveBytes(`in`: InputStream?, maxLength: Int): ByteArray {
        var total = 0
        val buffer = ByteArray(maxLength)
        var current: Int
        do {
            current = `in`!!.read(buffer, total, maxLength - total)
            if (current > 0) {
                total += current
            }
        } while (total < maxLength && current != -1)
        return buffer
    }

    override fun onDestroy() {
        mSerialPort.destroy()
        super.onDestroy()
    }

    fun start(view: View?) {
        mSerialPort.receive { fileInputStream: FileInputStream? ->
            try {
                if (fileInputStream!!.available() <= 0) {
                    return@receive
                }
                var buffer = ByteArray(1024)
                buffer = Arrays.copyOfRange(buffer, 0, fileInputStream.read(buffer))
                if (buffer[0] == (85).toByte() && buffer[1] == (-86).toByte() && buffer[3] == (0x00).toByte()) {
                    val len: Int =
                        (buffer[4] and (0xff).toByte()) + (buffer[5].toInt() shl 8 and 0xff)
                    if (len > 0) {
                        val result = ByteArray(len)
                        System.arraycopy(merge(buffer, receiveBytes(fileInputStream, len)), 6, result, 0, len)
                        Log.e("CCC", String(result))
                        mSerialPort.stopReceive()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@receive
            }
        }
    }

    fun stop(view: View?) {}
}