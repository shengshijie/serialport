package com.shengshijie.serialporttest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.shengshijie.serialport.SerialPort;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SerialPortActivity extends Activity {

    private SerialPort mSerialPort = new SerialPort();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);
        mSerialPort.init(new File("dev/ttyS4"), 115200, 0);
    }

    private byte[] merge(byte[] hand, byte[] tail) {
        if (hand == null) {
            return tail;
        }
        byte[] data3 = new byte[hand.length + tail.length];
        System.arraycopy(hand, 0, data3, 0, hand.length);
        System.arraycopy(tail, 0, data3, hand.length, tail.length);
        return data3;
    }

    private byte[] receiveBytes(InputStream in, int maxLength) throws IOException {
        int total = 0;
        byte[] buffer = new byte[maxLength];
        int current;
        do {
            current = in.read(buffer, total, maxLength - total);
            if (current > 0) {
                total += current;
            }
        } while (total < maxLength && current != -1);
        return buffer;
    }

    @Override
    protected void onDestroy() {
        mSerialPort.destroy();
        super.onDestroy();
    }

    public void start(View view) {
        mSerialPort.receive(new Function1<FileInputStream, Unit>() {
            @Override
            public Unit invoke(FileInputStream fileInputStream) {
                try {
                    if (fileInputStream.available() <= 0) {
                        return null;
                    }
                    byte[] buffer = new byte[1024];
                    buffer = Arrays.copyOfRange(buffer, 0, fileInputStream.read(buffer));
                    if (buffer[0] == 85 && buffer[1] == -86 && buffer[3] == 0x00) {
                        int len = (buffer[4] & 0xff) + ((buffer[5] << 8) & 0xff);
                        if (len > 0) {
                            byte[] result = new byte[len];
                            System.arraycopy(merge(buffer, receiveBytes(fileInputStream, len)), 6, result, 0, len);
                            Log.e("CCC", new String(result));
                            mSerialPort.stopReceive();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return null;
            }
        });
    }

    public void stop(View view) {

    }

}
