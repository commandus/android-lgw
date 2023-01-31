package com.commandus.gui;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.commandus.lgw.LGW;
import com.commandus.lgw.LGWListener;
import com.commandus.lgw.Payload;
import com.commandus.lgw.R;
import com.commandus.lgw.SerialIO;
import com.commandus.serial.SerialErrorListener;
import com.commandus.serial.SerialPort;
import com.commandus.serial.SerialSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * create notification and queue serial data while activity is not in the foreground
 */
public class LGWService extends Service implements
        LGWListener, SerialIO, SerialErrorListener {

    private SerialSocket usbSerialSocket;

    public PayloadAdapter payloadAdapter;


    /**
     * Return opened USB COM port
     *
     * @return >0 file descriptor, 0- no USB device found, <0- system error while open port
     */
    public int getUSBPortFileDescriptor() {
        if (!connected || usbSerialSocket == null)
            return 0;
        return usbSerialSocket.serialPort.getPortNumber();
    }

    @Override
    public void onDisconnect() {
        // device disconnected
        onInfo("loraGatewayListener disconnect USB serial socket");
        connected = false; // ignore data,errors while disconnecting
        cancelNotification();
        if (usbSerialSocket != null) {
            usbSerialSocket = null;
        }
        onDisconnected();
    }

    class LGWServiceBinder extends Binder {
        LGWService getService() {
            return LGWService.this;
        }
    }

    private final Handler mainLooper;
    private final IBinder binder;
    private LGWListener listener;
    private SerialIO serialIO;

    public LGW lgw;
    public boolean connected;
    public boolean running;
    public int receiveCount;
    public int valueCount;

    /**
     * Lifecylce
     */
    public LGWService() {
        mainLooper = new Handler(Looper.getMainLooper());
        binder = new LGWServiceBinder();
        lgw = new LGW();
        payloadAdapter = new PayloadAdapter();
        receiveCount = 0;
        valueCount = 0;
    }

    @Override
    public void onDestroy() {
        cancelNotification();
        disconnectSerialPort();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * @return false- permission denied
     */
    public boolean connectSerialPort() {
        onInfo(getString(R.string.msg_connecting_usb_serial_port));
        usbSerialSocket = SerialPort.open(this);
        if (usbSerialSocket == null) {
            onInfo(getString(R.string.err_open_serial_port) + SerialPort.reason);
            return false;
        }
        onConnected(usbSerialSocket.connect(this));
        return connected;
    }

    public void disconnectSerialPort() {
        onInfo("loraGatewayListener disconnect USB serial socket");
        connected = false; // ignore data,errors while disconnecting
        cancelNotification();
        if (usbSerialSocket != null) {
            SerialPort.close(usbSerialSocket);
            usbSerialSocket = null;
        }
        onDisconnected();
    }

    public void attach(LGWListener listener) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new IllegalArgumentException("not in main thread");
        cancelNotification();
        synchronized (this) {
            this.listener = listener;
        }
    }

    public static final String LOG_FILE_NAME = "lgw.log";

    private void log2file(String message) {
        if (true) {
            try {
                File docs = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "/lgw_com.log");
                FileOutputStream output = new FileOutputStream(docs.getAbsoluteFile(), true);
                output.write((new Date().toString() + ": " + message + "\n").getBytes());
                output.close();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onInfo(
        String message
    ) {
        log2file(message);

        synchronized (this) {
            if (listener != null) {
                mainLooper.post(() -> {
                    if (listener != null) {
                        listener.onInfo(message);
                    }
                });
            }
        }
    }

    @Override
    public void onConnected(boolean on) {
        connected = on;
        synchronized (this) {
            mainLooper.post(() -> {
                if (listener != null) {
                    listener.onConnected(on);
                }
            });
        }
    }

    @Override
    public void onDisconnected() {
        connected = false;
        running = false;
        synchronized (this) {
            mainLooper.post(() -> {
                if (listener != null) {
                    listener.onDisconnected();
                }
            });
        }
    }

    @Override
    public void onStarted(String gatewayId, String regionName, int regionIndex) {
        running = true;
        synchronized (this) {
            mainLooper.post(() -> {
                if (listener != null) {
                    listener.onStarted(gatewayId, regionName, regionIndex);
                }
            });
        }
    }

    @Override
    public void onFinished(String message) {
        running = false;
        synchronized (this) {
            mainLooper.post(() -> {
                if (listener != null) {
                    listener.onFinished(message);
                }
            });
        }
    }

    @Override
    public byte[] onRead(int bytes) {
        if (usbSerialSocket != null) {
            return usbSerialSocket.read(bytes);
        }
        return new byte[0];
    }

    @Override
    public int onWrite(byte[] data) {
        if (usbSerialSocket == null)
            return -1; // error
        return usbSerialSocket.write(data);
    }

    @Override
    public int onSetAttr(
        boolean blocking
    ) {
        usbSerialSocket.setBlocking(blocking);
        return 0;
    }

    @Override
    public void onReceive(Payload payload) {
        receiveCount++;
        synchronized (this) {
            mainLooper.post(() -> {
                if (listener != null) {
                    listener.onReceive(payload);
                }
            });
        }
    }

    @Override
    public void onValue(Payload payload) {
        valueCount++;
        synchronized (this) {
            mainLooper.post(() -> {
                if (listener != null) {
                    listener.onValue(payload);
                }
            });
        }
    }

    private void cancelNotification() {
        listener = null;
    }

    @SuppressLint("HardwareIds")
    boolean startGateway(int regionIndex, int verbosity) {
        if (lgw == null)
            return false;
        lgw.setPayloadListener(this);
        return lgw.start(regionIndex, Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID), verbosity) == 0;
    }

    void stopGateway() {
        if (lgw == null)
            return;
        lgw.stop();
        lgw.setPayloadListener(null);
    }

    String[] regionNames() {
        if (lgw == null)
            return null;
        return lgw.regionNames();
    }
}
