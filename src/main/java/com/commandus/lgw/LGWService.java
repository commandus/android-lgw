package com.commandus.lgw;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.commandus.ftdi.FTDI;
import com.hoho.android.usbserial.driver.UsbSerialPort;

/**
 * create notification and queue serial data while activity is not in the foreground
 */
public class LGWService extends Service
        implements PayloadListener {

    private static final String TAG = "lgw-service";;
    private UsbSerialPort usbSerialPort;

    class SerialBinder extends Binder {
        LGWService getService() {
            return LGWService.this;
        }
    }

    private final Handler mainLooper;
    private final IBinder binder;

    private boolean connected;

    private PayloadListener listener;

    /**
     * Lifecylce
     */
    public LGWService() {
        log("serial service created");
        mainLooper = new Handler(Looper.getMainLooper());
        binder = new SerialBinder();
    }

    @Override
    public void onDestroy() {
        cancelNotification();
        disconnect();
        log("serial service destroyed");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        log("serial service bind");
        return binder;
    }

    /**
     * Api
     */
    public void connect() {
        log("service connect serial port");
        usbSerialPort = FTDI.open(this);
        connected = true;
    }

    public void disconnect() {
        log("service disconnect serial socket");
        connected = false; // ignore data,errors while disconnecting
        cancelNotification();
        if(usbSerialPort != null) {
            FTDI.close(usbSerialPort);
            usbSerialPort = null;
        }
    }


    public void attach(PayloadListener listener) {
        log("attach serial listener");
        if (Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new IllegalArgumentException("not in main thread");
        cancelNotification();
        synchronized (this) {
            this.listener = listener;
            // parser.setDtSeconds(0);
        }
    }

    public void detach() {
        log("detach service, connected: " + Boolean.toString(connected));
        listener = null;
    }

    private void log(
            final String message
    ) {
        Log.d(TAG, message);
        synchronized (this) {
            if (listener != null) {
                mainLooper.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onInfo(message);
                        }
                    }
                });
            }
        }
    }

    private void cancelNotification() {
        // stopForeground(true);
    }

    @Override
    public void onValue(final Payload value) {
        synchronized (this) {
            if (listener != null) {
                mainLooper.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onValue(value);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onInfo(String msg) {
        synchronized (this) {
            if (listener != null) {
                mainLooper.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onInfo(msg);
                        }
                    }
                });
            }
        }
    }
    boolean startGateway() {
        return true;
    }

    boolean stopGateway() {
        return true;
    }

}
