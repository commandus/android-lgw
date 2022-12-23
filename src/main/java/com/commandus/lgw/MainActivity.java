package com.commandus.lgw;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.TextView;

import com.commandus.ftdi.FTDI;
import com.commandus.lgw.databinding.ActivityMainBinding;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
    implements ServiceConnection, PayloadListener
{
    private static final int SOUND_PRIORITY_1 = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

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
    private Settings settings;
    private BroadcastReceiver broadcastReceiver;
    private PayloadAdapter payloadAdapter;

    private TextView tvDevice;
    private Switch switchGateway;
    private TextView textStatusService;
    private TextView textStatusUSB;
    private TextView textStatusLGW;
    private RecyclerView recyclerViewLog;

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((LGWService.LGWServiceBinder) binder).getService();
        service.attach(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textStatusService.setText("USB");
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textStatusService.setText("usb");
                setUIUSBConnected(false);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = Settings.getSettings(this);
        // do not turn off screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        tvDevice = binding.textDevice;
        switchGateway = binding.switchGateway;
        textStatusService = binding.textStatusService;
        textStatusUSB = binding.textStatusUSB;
        textStatusLGW = binding.textStatusLGW;
        recyclerViewLog = binding.recyclerViewLog;

        tvDevice.setText(stringFromJNI());
        switchGateway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchGateway.isChecked()) {
                    switchGateway.setChecked(startLGW());
                } else {
                    stopLGW();
                }
            }
        });
        payloadAdapter = new PayloadAdapter();
        recyclerViewLog.setAdapter(payloadAdapter);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                doUSBAction(intent.getAction());
            }
        };

        startService(new Intent(this, LGWService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change

        checkTheme();
    }

    private void stopLGW() {
        if (connected && service != null) {
            service.stopGateway();
        }
    }

    private boolean startLGW() {
        if (connected && service != null) {
            return service.startGateway();
        } else
            return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        log(new Date().toString());
        bindService(new Intent(this, LGWService.class), this, Context.BIND_AUTO_CREATE);
        IntentFilter f = new IntentFilter();
        f.addAction(Settings.INTENT_ACTION_GRANT_USB);
        f.addAction(ACTION_USB_ATTACHED);
        f.addAction(ACTION_USB_DETACHED);
        registerReceiver(broadcastReceiver, f);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (service != null) {
            try {
                unbindService(this);
            } catch (Exception ignored) {

            }
        }
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        doUSBAction(intent.getAction());
        super.onNewIntent(intent);
    }

    private void doUSBAction(String action) {
        if (ACTION_USB_ATTACHED.equals(action)) {
            soundPool.play(SOUND_ON, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
            connectUSB();
        }
        if (ACTION_USB_DETACHED.equals(action)) {
            soundPool.play(SOUND_OFF, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
            log("USB device detached");
            if (service != null)
                service.stopGateway();
            setUIUSBConnected(false);
        }
    }

    /**
     * A native method that is implemented by the 'lgw' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    void log(String s) {
        payloadAdapter.push(s);
        Log.d(TAG, s);
    }

    @Override
    public void onValue(Payload value) {

    }

    @Override
    public void onInfo(String msg) {

    }

    @Override
    public void onConnected(boolean on) {
        if (on)
            log("connected");
        else
            log("disconnected");
        if (on)
            textStatusUSB.setText("Connected");
        else
            textStatusUSB.setText("Disconnected");
    }

    @Override
    public void onDisconnected() {
        textStatusUSB.setText("Disconnected");
    }

    private void connectUSB() {
        connectUSB(null);
    }

    private void connectUSB(Boolean permissionGranted) {
        log("try connect to USB");
        if (service == null)
            return;
        /*
        if (!service.connected) {
            service.connect();
            log("try service connect to USB");
            validateUSBConnection(permissionGranted);
            tryToFindDeviceInFuture(permissionGranted, 5000);
            return;
        }
        log("USB connection established");
         */

        if (FTDI.hasDevice(this)) {
            try {
                service.connect();
            } catch (Exception e) {
                log("connection failed: " + e.getMessage());
            }
        } else {
            log("Unknown USB device");
        }

        if (!service.connected)
            return;

        // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
        // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
        soundPool.play(SOUND_ON, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
    }

    private void setUIUSBConnected(boolean connected) {
        if (connected) {
            log("usb gateway device plugged in");
            textStatusUSB.setText("Connected");
            switchGateway.setChecked(false);
            switchGateway.setEnabled(false);
        } else {
            log("usb gateway device off");
            textStatusUSB.setText("Disconnected");
            switchGateway.setEnabled(true);
        }
    }

    private void tryToFindDeviceInFuture(final Boolean permissionGranted, int ms) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectUSB(permissionGranted);
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

    private void checkTheme() {
        if (settings.getTheme() == "dark")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

}