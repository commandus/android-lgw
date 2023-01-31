package com.commandus.gui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.commandus.lgw.DeviceAddresses;
import com.commandus.lgw.LGWListener;
import com.commandus.lgw.LgwSettings;
import com.commandus.lgw.Payload;
import com.commandus.lgw.R;
import com.commandus.lgw.databinding.ActivityMainBinding;
import com.commandus.serial.SerialPort;

public class MainActivity extends AppCompatActivity
    implements ServiceConnection, LGWListener, RegionDialog.RegionSelectListener
{
    private static final int SOUND_PRIORITY_1 = 1;
    private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    private int SOUND_ALARM;
    private int SOUND_BEEP;
    private int SOUND_OFF;
    private int SOUND_ON;
    private int SOUND_SHOT;

    private SoundPool soundPool;
    private LGWService service;
    private LgwSettings lgwSettings;
    private BroadcastReceiver broadcastReceiver;

    private Button buttonRegion;
    private SwitchCompat switchGateway;
    private TextView textStatusUSB;
    private TextView textStatusLGW;
    private TextView textCountRead;
    private RecyclerView recyclerViewLog;

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((LGWService.LGWServiceBinder) binder).getService();
        service.attach(this);
        recyclerViewLog.setAdapter(service.payloadAdapter);

        runOnUiThread(() -> {
            updateUiRegion();
            @SuppressLint("HardwareIds") String gwId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            setTitle(getString(R.string.app_name) + " " + gwId);
            // reflect does USB gateway connected already
            reflectUSBConnected(service.connected);
            reflectGatewayRunning(service.running);
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        recyclerViewLog.setAdapter(null);
        service = null;
        runOnUiThread(this::onDisconnected);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lgwSettings = LgwSettings.getSettings(this);
        // do not turn off screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        com.commandus.lgw.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        /*
        soundPool = new SoundPool.Builder()
            .setAudioAttributes(attributes)
            .setMaxStreams(5)
            .build();
        // load sounds
        // TODO suppress warning E/OMXMaster: A component of name 'OMX.qcom.audio.decoder.aac' already exists, ignoring this one.
        SOUND_ALARM = soundPool.load(this, R.raw.alarm, SOUND_PRIORITY_1);
        SOUND_BEEP = soundPool.load(this, R.raw.beep, SOUND_PRIORITY_1);
        SOUND_OFF = soundPool.load(this, R.raw.off, SOUND_PRIORITY_1);
        SOUND_ON = soundPool.load(this, R.raw.on, SOUND_PRIORITY_1);
        SOUND_SHOT = soundPool.load(this, R.raw.shot, SOUND_PRIORITY_1);
         */

        buttonRegion = binding.buttonRegion;
        Button buttonDevices = binding.buttonDevices;
        switchGateway = binding.switchGateway;
        textStatusUSB = binding.textStatusUSB;
        textStatusLGW = binding.textStatusLGW;
        textCountRead = binding.textLGWReadCount;
        // TextView textCountWrite = binding.textLGWWriteCount;

        // TextView textStatusLGW = binding.textStatusLGW;
        recyclerViewLog = binding.recyclerViewLog;

        DeviceAddresses deviceAddresses = new DeviceAddresses();
        int cnt = DeviceAddressProvider.count(this);
        if (cnt > 0)
            buttonDevices.setText(getString(R.string.label_button_device_count) + cnt);
        else
            buttonDevices.setText(R.string.label_button_devices);

        buttonDevices.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DevicesActivity.class);
            startActivity(intent);
        });

        switchGateway.setOnClickListener(view -> {
            if (switchGateway.isChecked()) {
                boolean r = startLGW();
                reflectGatewayRunning(r);
            } else {
                stopLGW();
            }
        });

        buttonRegion.setOnClickListener(view -> selectRegion());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                doUSBAction(intent);
            }
        };

        startService(new Intent(this, LGWService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change

        checkTheme();
    }

    private void selectRegion() {
        if (service == null)
            return;
        RegionDialog d = new RegionDialog(service.regionNames(), lgwSettings.getRegionIndex());
        d.show(getSupportFragmentManager(), "");
    }

    private void updateUiRegion() {
        if (service == null)
            return;
        int idx = lgwSettings.getRegionIndex();
        String[] rns = service.regionNames();
        if (idx >= 0 && idx < rns.length)
            buttonRegion.setText(rns[idx]);
    }

    private void stopLGW() {
        if (service != null) {
            service.stopGateway();
        }
        reflectGatewayRunning(false);
    }

    private boolean startLGW() {
        if (service == null)
            return false;

        if (!isUSBConnected()) {
            if (SerialPort.hasDevice(MainActivity.this)) {
                connectUSB();
            }
        }
        if (!isUSBConnected()) {
            onInfo(getString(R.string.message_no_usb_connection_established));
            return false;
        }

        int regionIndex = lgwSettings.getRegionIndex();
        int verbosity = lgwSettings.getVerbosity();
        return service.startGateway(regionIndex, verbosity);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindService(new Intent(this, LGWService.class), this, Context.BIND_AUTO_CREATE);
        IntentFilter f = new IntentFilter();
        f.addAction(LgwSettings.INTENT_ACTION_GRANT_USB);
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
        doUSBAction(intent);
        super.onNewIntent(intent);
    }

    private void doUSBAction(Intent intent) {
        String action = intent.getAction();
        if (LgwSettings.INTENT_ACTION_GRANT_USB.equals(action)) {
            boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
            if (granted) {
                onInfo("USB device access permission granted");
                connectUSB();
            } else {
                onInfo("USB device access permission denied, quit..");
            }
        }
        if (ACTION_USB_ATTACHED.equals(action)) {
            if (SerialPort.hasDevice(this)) {
                onInfo("USB device attached");
                // soundPool.play(SOUND_ON, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
                connectUSB();
            }
        }
        if (ACTION_USB_DETACHED.equals(action)) {
            // soundPool.play(SOUND_OFF, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
            onInfo("USB device detached");
            disconnectUSB();
        }
    }

    private void pushMessage(String msg) {
        if (service == null)
            return;
        service.payloadAdapter.push(msg);
        recyclerViewLog.smoothScrollToPosition(service.payloadAdapter.logData.size() - 1);
    }

    @Override
    public void onStarted(String gatewayId, String regionName, int regionIndex) {
        pushMessage(getString(R.string.label_running));
        reflectGatewayRunning(true);
    }

    private void reflectGatewayRunning(boolean on) {
        switchGateway.setChecked(on);
        textStatusLGW.setText(on ? R.string.label_running : R.string.label_stopped);
        switchGateway.setText(on ? R.string.gateway_on : R.string.gateway_off);
    }

    @Override
    public void onFinished(String message) {
        pushMessage(getString(R.string.label_stopped));
        reflectGatewayRunning(false);
        pushMessage(getString(R.string.msg_finished) + message);
        // soundPool.play(SOUND_BEEP, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
    }

    @Override
    public void onValue(Payload value) {
        pushMessage(value.hexPayload);
        textCountRead.setText(Integer.toString(service.valueCount));
    }

    @Override
    public void onInfo(String msg)
    {
        Log.d("main", msg);
        pushMessage(msg);
    }

    @Override
    public void onConnected(boolean on) {
        pushMessage(getString(R.string.label_connected));
        reflectUSBConnected(on);
    }

    @Override
    public void onDisconnected() {
        pushMessage(getString(R.string.label_disconnected));
        reflectUSBConnected(false);
        // soundPool.play(SOUND_ALARM, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
    }

    private void disconnectUSB() {
        if (service != null)
            service.stopGateway();
        reflectUSBConnected(false);
    }
    private boolean isUSBConnected() {
        if (service == null)
            return false;
        return service.connected;
    }

    private void connectUSB() {
        if (service == null) {
            onInfo(getString(R.string.msg_no_service));
            reflectUSBConnected(false);
            return;
        }
        if (service.connected) {
            onInfo(getString(R.string.msg_already_connected));
            reflectUSBConnected(true);
            return;
        }
        if (SerialPort.hasDevice(this)) {
            if (!service.connectSerialPort()) // permission not granted
                return;
        } else {
            onInfo(getString(R.string.msg_unknown_usb_device));
        }
        reflectUSBConnected(service.connected);
        if (service.connected) {
            // soundPool.play(SOUND_ON, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
        }
    }

    private void reflectUSBConnected(boolean connected) {
        if (connected) {
            textStatusUSB.setText(getString(R.string.label_connected));
            switchGateway.setEnabled(true);
        } else {
            textStatusUSB.setText(getString(R.string.label_disconnected));
            reflectGatewayRunning(false);
            switchGateway.setEnabled(false);
        }
    }

    private void checkTheme() {
        if (lgwSettings.getTheme().equals(getString(R.string.theme_name_dark)))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void onSetRegionIndex(int selection) {
        if (lgwSettings.getRegionIndex() != selection) {
            // soundPool.play(SOUND_SHOT, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
            lgwSettings.setRegionIndex(selection);
            lgwSettings.save();
        }
        updateUiRegion();
    }

}