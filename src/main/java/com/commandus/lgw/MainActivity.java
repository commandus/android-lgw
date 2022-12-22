package com.commandus.lgw;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.commandus.ftdi.FTDI;
import com.commandus.lgw.databinding.ActivityMainBinding;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
    implements PayloadListener
{
    private static final int SOUND_PRIORITY_1 = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private int SOUND_ALARM;
    private int SOUND_BEEP;
    private int SOUND_OFF;
    private int SOUND_ON;
    private int SOUND_SHOT;

    // Used to load the 'lgw' library on application startup.
    static {
        System.loadLibrary("lgw");
    }

    private ActivityMainBinding binding;
    private SoundPool soundPool;

    private LGWService service;
    private boolean connected = false;

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        log("service connect..");
        service = ((LGWService.SerialBinder) binder).getService();
        service.attach(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectUSBNSendMeasureAmbient();
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        log("disconnected");
        service = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(10)
                .build();
        // load sounds
        SOUND_ALARM = soundPool.load(this, R.raw.alarm, SOUND_PRIORITY_1);
        SOUND_BEEP = soundPool.load(this, R.raw.beep, SOUND_PRIORITY_1);
        SOUND_OFF = soundPool.load(this, R.raw.off, SOUND_PRIORITY_1);
        SOUND_ON = soundPool.load(this, R.raw.on, SOUND_PRIORITY_1);
        SOUND_SHOT = soundPool.load(this, R.raw.shot, SOUND_PRIORITY_1);

        // Example of a call to a native method
        TextView tvDevice = binding.textDevice;
        tvDevice.setText(stringFromJNI());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
            soundPool.play(SOUND_ON, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
            log("new intent USB_DEVICE_ATTACHED, new device attached");
            log("trying to connect");
            connectUSBNSendMeasureAmbient();
        }
        super.onNewIntent(intent);
    }

    /**
     * A native method that is implemented by the 'lgw' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    void log(String s) {
        Log.d(TAG, s);
    }

    @Override
    public void onValue(Payload value) {

    }

    @Override
    public void onInfo(String msg) {

    }

    private void connectUSBNSendMeasureAmbient() {
        connectUSBNSendMeasureAmbient(null);
    }

    private void connectUSBNSendMeasureAmbient(Boolean permissionGranted) {
        if (usbConnection == null) {
            validateUSBConnection(permissionGranted);
            if (usbConnection == null) {
                tryToFindDeviceInFuture(permissionGranted, 5000);
                return;
            }
        }

        log("USB connection established..");

        if (!FTDI.hasDevice(this)) {
            log("connect ..");
            try {
                service.connect();
            } catch (Exception e) {
                log("connect exception: " + e.getMessage());
            }
        }

        // start gateway
        service.startGateway();

        // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
        // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
        soundPool.play(SOUND_ON, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
    }

    private void tryToFindDeviceInFuture(final Boolean permissionGranted, int ms) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectUSBNSendMeasureAmbient(permissionGranted);
                    }
                });
            }
        }, ms);
    }

    /**
     * Set usbConnection, usbDriver
     * @param permissionGranted
     */
    private void validateUSBConnection(Boolean permissionGranted) {

    }

}