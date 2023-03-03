package com.commandus.lgw;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.commandus.serial.SerialErrorListener;
import com.commandus.serial.SerialPort;
import com.commandus.serial.SerialSocket;

/**
 * create notification and queue serial data while activity is not in the foreground
 */
public class LGWService extends Service implements
        LGWListener, SerialErrorListener {

    private SerialSocket usbSerialSocket;

    public GatewayEventAdapter gatewayEventAdapter;
    // where to send payload
    private Uri mContentProviderUri;

    @Override
    public void onDisconnectUsb() {
        // device disconnected
        // try to shutdown
        stopGateway();
        showError(0, "loraGatewayListener disconnect USB serial socket");
        isUsbConnected = false; // ignore data,errors while disconnecting
        cancelNotification();
        if (usbSerialSocket != null) {
            usbSerialSocket = null;
        }
        onUsbDisconnected();
    }

    public void setContentProviderUri(String contentProvider) {
        mContentProviderUri = Uri.parse(contentProvider);
    }

    public boolean checkIsUSBConnected() {
        if (isUsbConnected)
            return true;
        if (SerialPort.hasDevice(this))
            usbSerialSocket = SerialPort.open(this);
        isUsbConnected = usbSerialSocket != null;
        return isUsbConnected;
    }

    class LGWServiceBinder extends Binder {
        LGWService getService() {
            return LGWService.this;
        }
    }

    private final Handler mainLooper;
    private final IBinder binder;
    private LGWListener listener;

    public LoraWanGateway lgw;
    public boolean isUsbConnected = false;
    public boolean running;
    public int receiveCount;
    public int valueCount;

    public LGWService() {
        mainLooper = new Handler(Looper.getMainLooper());
        binder = new LGWServiceBinder();
        boolean fakeGateway = false;
        if (fakeGateway)
            lgw = new LorawanGatewayFake();
        else
            lgw = new LorawanGatewayRak2287();
        gatewayEventAdapter = new GatewayEventAdapter();
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
        showError(0, getString(R.string.msg_connecting_usb_serial_port));
        usbSerialSocket = SerialPort.open(this);
        isUsbConnected = usbSerialSocket != null;
        if (!isUsbConnected) {
            showError(0, getString(R.string.err_open_serial_port) + SerialPort.reason);
            return false;
        }

        onUsbConnected(usbSerialSocket.connect(this));
        return isUsbConnected;
    }

    public void disconnectSerialPort() {
        showError(0, "loraGatewayListener disconnect USB serial socket");
        isUsbConnected = false; // ignore data,errors while disconnecting
        cancelNotification();
        if (usbSerialSocket != null) {
            SerialPort.close(usbSerialSocket);
            usbSerialSocket = null;
        }
        onUsbDisconnected();
    }

    public void attach(LGWListener listener) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new IllegalArgumentException("not in main thread");
        cancelNotification();
        synchronized (this) {
            this.listener = listener;
        }
    }

    @Override
    public void onInfo(
        int severity,
        String message
    ) {
        boolean fatal = message.startsWith("Error");
        if (fatal)
            severity = 1;
        showError(severity, message);
        if (fatal)
            stopGateway();
    }

    private void showError(int severity, String message) {
        Log.e(LogHelper.TAG, message);
        synchronized (this) {
            if (listener != null) {
                mainLooper.post(() -> {
                    if (listener != null) {
                        listener.onInfo(severity, message);
                    }
                });
            }
        }
    }

    @Override
    public void onUsbConnected(boolean on) {
        isUsbConnected = on;
        synchronized (this) {
            mainLooper.post(() -> {
                if (listener != null) {
                    listener.onUsbConnected(on);
                }
            });
        }
    }

    @Override
    public void onUsbDisconnected() {
        synchronized (this) {
            mainLooper.post(() -> {
                if (listener != null) {
                    listener.onUsbDisconnected();
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
    public LoraDeviceAddress onIdentityGet(String devAddr) {
        return DeviceAddressProvider.getByAddress(this, devAddr);
    }

    @Override
    public LoraDeviceAddress onGetNetworkIdentity(String devEui) {
        return DeviceAddressProvider.getByDevEui(this, devEui);
    }

    @Override
    public int onIdentitySize() {
        return DeviceAddressProvider.count(this);
    }

    @Override
    public void onReceive(Payload payload) {
        receiveCount++;
        synchronized (this) {
            mainLooper.post(() -> {
                gatewayEventAdapter.pushReceived(payload);
                if (listener != null) {
                    listener.onReceive(payload);
                }
            });
        }
    }

    @Override
    public void onValue(Payload payload) {
        valueCount++;
        Uri uri = sendPayload2ContentProvider(payload);
        if (uri == null)
            return; // smth wrong
        payload.id = ContentUris.parseId(uri);
        synchronized (this) {
            mainLooper.post(() -> {
                gatewayEventAdapter.pushPayLoad(payload);
                if (listener != null) {
                    listener.onValue(payload);
                }
            });
        }
    }

    private Uri sendPayload2ContentProvider(Payload payload) {
        if (mContentProviderUri == null)
            return null;
        try {
            return getContentResolver().insert(mContentProviderUri,
                payload.getContentValues());
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    private void cancelNotification() {
        listener = null;
    }

    @SuppressLint("HardwareIds")
    boolean startGateway(int regionIndex, int verbosity) {
        if (lgw == null)
            return false;
        lgw.assignPayloadListener(this);
        return lgw.startGateway(regionIndex, Settings.Secure.getString(getContentResolver(),
            Settings.Secure.ANDROID_ID), verbosity) == 0;
    }

    void stopGateway() {
        if (lgw == null)
            return;
        lgw.stopGateway();
    }

    String[] regionNames() {
        if (lgw == null)
            return null;
        return lgw.getRegionNames();
    }
}
