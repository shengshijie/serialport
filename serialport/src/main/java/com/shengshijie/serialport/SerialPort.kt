package com.shengshijie.serialport

import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.concurrent.thread

class SerialPort {

    init {
        System.loadLibrary("serial_port")
    }

    private var mFd: FileDescriptor? = null
    private var fileInputStream: FileInputStream? = null
    private var fileOutputStream: FileOutputStream? = null
    private external fun open(absolutePath: String, baudRate: Int, flags: Int): FileDescriptor?
    private var mReadThread: Thread? = null
    private var isReading: Boolean = false

    private external fun close()

    @JvmOverloads
    fun init(
        device: File,
        baudRate: Int,
        flags: Int,
        result: (isSuccess: Boolean, errorMsg: String) -> Unit = { _, _ -> }
    ) {
        if (!device.canRead() || !device.canWrite()) {
            try {
                val su: Process = Runtime.getRuntime().exec("/system/bin/su")
                val cmd = "chmod 666 " + device.absolutePath + "\n" + "exit\n"
                su.outputStream.write(cmd.toByteArray())
                if (su.waitFor() != 0 || !device.canRead() || !device.canWrite()) {
                    result(false, "permission denied")
                    return
                }
            } catch (e: Exception) {
                result(false, e.message.toString())
                return
            }
        }
        mFd = open(device.absolutePath, baudRate, flags)
        if (mFd == null) {
            result(false, "mFd open failed")
            return
        }
        fileOutputStream = FileOutputStream(mFd!!)
        fileInputStream = FileInputStream(mFd!!)
        result(true, "")
    }

    fun send(bytes: ByteArray) {
        fileOutputStream?.write(bytes)
    }

    fun receive(callback: (FileInputStream?) -> Unit) {
        if (isReading) return
        isReading = true
        mReadThread = thread {
            while (!mReadThread!!.isInterrupted) {
                try {
                    fileInputStream?.let { callback(fileInputStream) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopReceive() {
        isReading = false
        mReadThread?.interrupt()
    }

    fun destroy() {
        close()
    }

}