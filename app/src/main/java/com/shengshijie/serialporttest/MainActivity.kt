package com.shengshijie.serialporttest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shengshijie.serialport.SerialPortFinder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        finish()
        startActivity(Intent(this,SerialPortActivity::class.java))
        val allDevices = SerialPortFinder().allDevices
        println("devices::"+allDevices.contentToString())
    }
}
