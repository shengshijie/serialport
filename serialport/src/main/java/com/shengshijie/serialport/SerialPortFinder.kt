package com.shengshijie.serialport

import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.LineNumberReader
import java.util.*

class SerialPortFinder {
    inner class Driver(val name: String, private val mDeviceRoot: String) {
        var mDevices: Vector<File>? = null
        fun getDevices(): Vector<File> {
            if (mDevices == null) {
                mDevices = Vector()
                val dev = File("/dev")
                val files = dev.listFiles()
                var i: Int
                i = 0
                while (i < files.size) {
                    if (files[i].absolutePath.startsWith(mDeviceRoot)) {
                        mDevices!!.add(files[i])
                    }
                    i++
                }
            }
            return mDevices!!
        }

    }

    private var mDrivers: Vector<Driver>? =
        null

    @Throws(IOException::class)
    fun getDrivers(): Vector<Driver> {
        if (mDrivers == null) {
            mDrivers = Vector()
            val r =
                LineNumberReader(FileReader("/proc/tty/drivers"))
            var l: String
            while (r.readLine().also { l = it } != null) {
                val drivername = l.substring(0, 0x15).trim { it <= ' ' }
                val w = l.split(" +").toTypedArray()
                if (w.size >= 5 && w[w.size - 1] == "serial") {
                    mDrivers!!.add(Driver(drivername, w[w.size - 4])
                    )
                }
            }
            r.close()
        }
        return mDrivers!!
    }

    fun getAllDevices(): Array<String> {
        val devices = Vector<String>()
        val itdriv: Iterator<Driver>
        try {
            itdriv = getDrivers().iterator()
            while (itdriv.hasNext()) {
                val driver = itdriv.next()
                val itdev: Iterator<File> = driver.getDevices().iterator()
                while (itdev.hasNext()) {
                    val device = itdev.next().name
                    val value =
                        String.format("%s (%s)", device, driver.name)
                    devices.add(value)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return devices.toTypedArray()
    }

    fun getAllDevicesPath(): Array<String> {
        val devices = Vector<String>()
        val itdriv: Iterator<Driver>
        try {
            itdriv = getDrivers().iterator()
            while (itdriv.hasNext()) {
                val driver = itdriv.next()
                val itdev: Iterator<File> = driver.getDevices().iterator()
                while (itdev.hasNext()) {
                    val device = itdev.next().absolutePath
                    devices.add(device)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return devices.toTypedArray()
    }

}