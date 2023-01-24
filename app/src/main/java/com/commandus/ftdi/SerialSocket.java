package com.commandus.ftdi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

import com.commandus.lgw.LgwHelper;
import com.commandus.lgw.LgwSettings;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public class SerialSocket {
    private final BroadcastReceiver disconnectBroadcastReceiver;

    private final Context context;
    public SerialErrorListener listener;
    public UsbDeviceConnection connection;
    public UsbSerialPort serialPort;

    private int readWriteTimeout = 100; // ms
    private ByteBuffer mReadBuffer;
    private int mCountReadBuffer;
    private int mPositionReadBuffer;
    private final Object mReadBufferLock = new Object();

    public byte[] read(int bytes) {
        return read(bytes, readWriteTimeout);
    }

    /**
     * Read USB COM port
     * @param count buffer size
     * @param timeoutMs the timeout for writing in milliseconds, 0 is infinite
     * @return -1 - error, >0- count of bytes
     */
    public byte[] read(int count, int timeoutMs) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c = getFromBuffer(baos, count);
        int remaining = count - c;
        if (remaining > 0) {
            readIntoBuffer(timeoutMs);
            getFromBuffer(baos, remaining);
        }
        byte[] r = baos.toByteArray();
        return r;
    }

    private int getFromBuffer(ByteArrayOutputStream os, int count) {
        int sz = 0;
        if (mCountReadBuffer > 0) {
            // get from the buffer
            int cnt = mCountReadBuffer - mPositionReadBuffer;
            if (cnt <= 0) {
                // reset
                mCountReadBuffer = 0;
                mPositionReadBuffer = 0;
            } else {
                // min size
                sz = Math.min(count, cnt);
                os.write(mReadBuffer.array(),  mPositionReadBuffer, sz);
                mPositionReadBuffer += sz;
            }
        }
        return sz;
    }

    /**
     * Read USB COM port, set mBytesReadBuffer
     * @param timeoutMs the timeout for writing in milliseconds, 0 is infinite
     */
    private void readIntoBuffer(int timeoutMs) {
        mPositionReadBuffer = 0;
        byte[] buffer;
        synchronized (mReadBufferLock) {
            buffer = mReadBuffer.array();
        }
        try {
            mCountReadBuffer = serialPort.read(buffer, timeoutMs);
        } catch (IOException ignored) {
        }
    }

    public int write(byte[] buffer) {
        return write(buffer, readWriteTimeout);
    }

    /**
     * Write USB COM port
     * @param buffer the source byte buffer
     * @param timeoutMs the timeout for writing in milliseconds, 0 is infinite
     * @return bytes written
     */
    public int write(byte[] buffer, int timeoutMs) {
        try {
            serialPort.write(buffer, timeoutMs);
        } catch (IOException ignored) {
            return -1;
        }
        return buffer.length;
    }

    SerialSocket(Context context, UsbDeviceConnection connection, UsbSerialPort serialPort) {
        if (context instanceof Activity)
            throw new InvalidParameterException("expected non UI context");
        mReadBuffer = ByteBuffer.allocate(4096);
        mCountReadBuffer = 0;
        mPositionReadBuffer = 0;
        this.context = context;
        this.connection = connection;
        this.serialPort = serialPort;
        disconnectBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                disconnect(); // disconnect now, else would be queued until UI re-attached
                if (listener != null)
                    listener.onDisconnect();
            }
        };
    }

    String getName() { return serialPort.getDriver().getClass().getSimpleName().replace("SerialDriver",""); }

    public boolean connect(SerialErrorListener listener) {
        this.listener = listener;
        context.registerReceiver(disconnectBroadcastReceiver, new IntentFilter(LgwSettings.INTENT_ACTION_DISCONNECT));
        try {
            serialPort.setDTR(true); // for arduino, ...
            serialPort.setRTS(true);
            serialPort.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    public void disconnect() {
        listener = null; // ignore remaining data and errors
        if (serialPort != null) {
            try {
                serialPort.setDTR(false);
                serialPort.setRTS(false);
            } catch (Exception ignored) {
            }
            try {
                serialPort.close();
            } catch (Exception ignored) {
            }
            serialPort = null;
        }
        if (connection != null) {
            connection.close();
            connection = null;
        }
        try {
            context.unregisterReceiver(disconnectBroadcastReceiver);
        } catch (Exception ignored) {
        }
    }

    public void setBlocking(boolean blocking) {
        if (blocking)
            readWriteTimeout = 0;
        else
            readWriteTimeout = 100; // ms
    }
}
