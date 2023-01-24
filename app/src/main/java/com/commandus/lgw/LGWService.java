package com.commandus.lgw;

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

import com.commandus.ftdi.FTDI;
import com.commandus.ftdi.SerialErrorListener;
import com.commandus.ftdi.SerialSocket;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * create notification and queue serial data while activity is not in the foreground
 */
public class LGWService extends Service implements LGWListener, SerialErrorListener {

    private SerialSocket usbSerialSocket;
    /**
     * Return opened USB COM port
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

    public LGW lgw;
    public boolean connected;

    /**
     * Lifecylce
     */
    public LGWService() {
        mainLooper = new Handler(Looper.getMainLooper());
        binder = new LGWServiceBinder();
        lgw = new LGW();
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
     * */
    public boolean connectSerialPort() {
        onInfo("Connecting USB serial port..");
        usbSerialSocket = FTDI.open(this);
        if (usbSerialSocket == null) {
            onInfo("Error open serial port: " + FTDI.reason);
            return false;
        }
        connected = usbSerialSocket.connect(this);
        onConnected(connected);
        return connected;
    }

    public void disconnectSerialPort() {
        onInfo("loraGatewayListener disconnect USB serial socket");
        connected = false; // ignore data,errors while disconnecting
        cancelNotification();
        if (usbSerialSocket != null) {
            FTDI.close(usbSerialSocket);
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
            byte[] r = usbSerialSocket.read(bytes);
            // onInfo("Read " + r.length + " bytes: " + LgwHelper.bytesToHex(r) + ", buffer size: " + Integer.toString(bytes));
            if (listener != null) {
                synchronized (this) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onRead(bytes);
                        }
                    });
                }
            }
            return r;
        }
        return new byte[0];
    }

    @Override
    public int onWrite(byte[] data) {
        // onInfo("Write " + data.length + " bytes: " + LgwHelper.bytesToHex(data));
        if (usbSerialSocket == null)
            return -1; // error
        int r = usbSerialSocket.write(data);
        if (listener != null) {
            synchronized (this) {
                mainLooper.post(() -> {
                    if (listener != null) {
                        listener.onWrite(data);
                    }
                });
            }
        }
        return r;
    }

    @Override
    public int onSetAttr(
        boolean blocking
    ) {
        usbSerialSocket.setBlocking(blocking);
        return 0;
    }

    @Override
    public void onValue(Payload payload) {
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
