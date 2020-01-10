package com.shengshijie.serialport

import android.util.Log
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.LineNumberReader
import java.util.*

class SerialPortFinder {

    inner class Driver(val name: String, private val mDeviceRoot: String) {
        private var mDevices: Vector<File>? = null
        val devices: Vector<File>
            get() {
                if (mDevices == null) {
                    mDevices = Vector()
                    val dev = File("/dev")
                    val files = dev.listFiles()
                    var i = 0
                    while (i < files.size) {
                        if (files[i].absolutePath.startsWith(mDeviceRoot)) {
                            Log.d(TAG, "Found new device: " + files[i])
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

    val drivers: Vector<Driver>
        get() {
            if (mDrivers == null) {
                mDrivers = Vector()
                val r =
                    LineNumberReader(FileReader("/proc/tty/drivers"))
                var l: String
                while (r.readLine().also { l = it } != null) {
                    val drivername = l.substring(0, 0x15).trim { it <= ' ' }
                    val w = l.split(" +").toTypedArray()
                    if (w.size >= 5 && w[w.size - 1] == "serial") {
                        Log.d(TAG, "Found new driver " + drivername + " on " + w[w.size - 4])
                        mDrivers!!.add(Driver(drivername, w[w.size - 4]))
                    }
                }
                r.close()
            }
            return mDrivers!!
        }

    val allDevices: Array<String>
        get() {
            val devices = Vector<String>()
            val itdriv: Iterator<Driver>
            try {
                itdriv = drivers.iterator()
                while (itdriv.hasNext()) {
                    val driver = itdriv.next()
                    val itdev: Iterator<File> =
                        driver.devices.iterator()
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

    val allDevicesPath: Array<String>
        get() {
            val devices = Vector<String>()
            // Parse each driver
            val itdriv: Iterator<Driver>
            try {
                itdriv = drivers.iterator()
                while (itdriv.hasNext()) {
                    val driver = itdriv.next()
                    val itdev: Iterator<File> =
                        driver.devices.iterator()
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

    companion object {
        private const val TAG = "SerialPort"
    }
}