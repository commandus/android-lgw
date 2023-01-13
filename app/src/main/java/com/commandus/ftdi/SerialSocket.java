package com.commandus.ftdi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;

import java.security.InvalidParameterException;

import com.commandus.lgw.LgwSettings;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public class SerialSocket {

    private final BroadcastReceiver disconnectBroadcastReceiver;

    private final Context context;
    public SerialErrorListener listener;
    public UsbDeviceConnection connection;
    public UsbSerialPort serialPort;

    SerialSocket(Context context, UsbDeviceConnection connection, UsbSerialPort serialPort) {
        if(context instanceof Activity)
            throw new InvalidParameterException("expected non UI context");
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
}
