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
public class LGWService extends Service implements LogIntf
{

    private static final String TAG = "lgw-service";;
    private UsbSerialPort usbSerialPort;

    class LGWServiceBinder extends Binder {
        LGWService getService() {
            return LGWService.this;
        }
    }

    private final Handler mainLooper;
    private final IBinder binder;
    private PayloadListener listener;

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
     * Api
     */
    public void connectSerialPort() {
        log("lgw connect USB serial port");
        usbSerialPort = FTDI.open(this);
        connected = usbSerialPort != null;
        if (!connected) {
            log("lgw connect error " + FTDI.reason);
        }
        informConnected(connected);
    }

    public void disconnectSerialPort() {
        log("lgw disconnect USB serial socket");
        connected = false; // ignore data,errors while disconnecting
        cancelNotification();
        if(usbSerialPort != null) {
            FTDI.close(usbSerialPort);
            usbSerialPort = null;
        }
        informDisconnected();
    }

    public void attach(PayloadListener listener) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new IllegalArgumentException("not in main thread");
        cancelNotification();
        synchronized (this) {
            this.listener = listener;
        }
    }

    public void detach() {
        listener = null;
    }

    @Override
    public void log(
        String message
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

    public void informValue(final Payload value) {
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

    public void informConnected(boolean on) {
        synchronized (this) {
            if (listener != null) {
                mainLooper.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onConnected(on);
                        }
                    }
                });
            }
        }
    }

    public void informDisconnected() {
        synchronized (this) {
            if (listener != null) {
                mainLooper.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDisconnected();
                        }
                    }
                });
            }
        }
    }

    boolean startGateway(boolean connected, int fd) {
        if (lgw == null)
            return false;
        lgw.setLog(this);
        lgw.start(connected, fd);
        return true;
    }

    boolean stopGateway() {
        if (lgw == null)
            return false;
        lgw.stop();
        lgw.setLog(null);
        return true;
    }

}
