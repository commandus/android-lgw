package com.commandus.lgw;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import com.commandus.ftdi.FTDI;
import com.commandus.lgw.databinding.ActivityMainBinding;

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
    private PayloadAdapter payloadAdapter;
    private DeviceAddresses deviceAddresses;

    private Button buttonRegion;
    private TextView tvDevice;
    private Switch switchGateway;
    private TextView textStatusService;
    private TextView textStatusUSB;
    private Button buttonDevices;

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((LGWService.LGWServiceBinder) binder).getService();
        service.attach(this);
        runOnUiThread(() -> {
            updateUiRegion();
            @SuppressLint("HardwareIds") String gwId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            setTitle(getString(R.string.app_name) + " " + gwId);
            tvDevice.setText(R.string.label_device_ready);
            textStatusService.setText(R.string.label_usb_on);
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        runOnUiThread(() -> {
            textStatusService.setText(R.string.label_usb_off);
            tvDevice.setText(R.string.label_device_disconnect);
            setUIUSBConnected(false);
        });
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

        buttonRegion = binding.buttonRegion;
        buttonDevices = binding.buttonDevices;
        tvDevice = binding.textDevice;
        switchGateway = binding.switchGateway;
        textStatusService = binding.textStatusService;
        textStatusUSB = binding.textStatusUSB;

        // TextView textStatusLGW = binding.textStatusLGW;
        RecyclerView recyclerViewLog = binding.recyclerViewLog;

        deviceAddresses = new DeviceAddresses();
        int cnt = 0;
        if (cnt > 0) {
            buttonDevices.setText(getString(R.string.label_button_device_count) + cnt);
        } else {
            buttonDevices.setText(R.string.label_button_devices);
        }

        buttonDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DevicesActivity.class);
                startActivity(intent);
            }
        });

        switchGateway.setOnClickListener(view -> {
            if (switchGateway.isChecked()) {
                boolean r = startLGW();
                switchGateway.setChecked(r);
                switchGateway.setText(r ? R.string.gateway_on : R.string.gateway_off);
            } else {
                stopLGW();
            }
        });

        buttonRegion.setOnClickListener(view -> selectRegion());

        payloadAdapter = new PayloadAdapter();
        recyclerViewLog.setAdapter(payloadAdapter);

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
    }

    private boolean startLGW() {
        if (service == null)
            return false;

        if (!isUSBConnected()) {
            if (FTDI.hasDevice(MainActivity.this)) {
                connectUSB();
            }
        }
        if (!isUSBConnected())
            return false;

        int fd = service.getUSBPortFileDescriptor();
        int regionIndex = lgwSettings.getRegionIndex();
        if (fd < 0)
            return false;
        return service.startGateway(fd, regionIndex);
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
                onInfo("USB device access permission granted, reconnecting..");
                connectUSB();
            } else {
                onInfo("USB device access permission denied, quit..");
            }
        }
        if (ACTION_USB_ATTACHED.equals(action)) {
            if (FTDI.hasDevice(this)) {
                onInfo("USB device attached");
                soundPool.play(SOUND_ON, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
                connectUSB();
            }
        }
        if (ACTION_USB_DETACHED.equals(action)) {
            soundPool.play(SOUND_OFF, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
            onInfo("USB device detached");
            disconnectUSB();
        }
    }

    @Override
    public void onStarted(int fd, String gatewayId, String regionName, int regionIndex) {
        switchGateway.setText(R.string.gateway_on);
        payloadAdapter.push("Started " + gatewayId + " " + regionName);
    }

    @Override
    public void onFinished(String message) {
        switchGateway.setChecked(false);
        switchGateway.setText(R.string.gateway_off);
        payloadAdapter.push("Finished " + message);
        soundPool.play(SOUND_BEEP, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
    }

    @Override
    public void onValue(Payload value) {
        payloadAdapter.push(value.hexPayload);
    }

    @Override
    public void onInfo(String msg)
    {
        payloadAdapter.push(msg);
    }

    @Override
    public void onConnected(boolean on) {
        if (on)
            onInfo("connected");
        else
            onInfo("disconnected");
        if (on)
            textStatusUSB.setText(R.string.message_connected);
        else
            textStatusUSB.setText(R.string.message_disconnected);
    }

    @Override
    public void onDisconnected() {
        textStatusUSB.setText(R.string.message_disconnected);
        soundPool.play(SOUND_ALARM, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
    }

    private void disconnectUSB() {
        if (service != null)
            service.stopGateway();
        setUIUSBConnected(false);
    }
    private boolean isUSBConnected() {
        if (service == null)
            return false;
        return service.connected;
    }

    private void connectUSB() {
        onInfo("Connecting..");
        if (service == null) {
            setUIUSBConnected(false);
            return;
        }
        if (service.connected) {
            onInfo("Already connected");
            setUIUSBConnected(true);
            return;
        }
        if (FTDI.hasDevice(this)) {
            if (service.connectSerialPort()) // permission not granted
                return;
        } else {
            onInfo("Unknown USB device");
        }
        onInfo("Connected: " + service.connected);
        setUIUSBConnected(service.connected);
        if (service.connected)
            soundPool.play(SOUND_ON, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
        if (service.connected)
            onInfo("Successfully connected");
    }

    private void setUIUSBConnected(boolean connected) {
        if (connected) {
            textStatusUSB.setText(R.string.message_connected);
            switchGateway.setChecked(false);
            switchGateway.setEnabled(false);
        } else {
            textStatusUSB.setText(R.string.message_disconnected);
            switchGateway.setEnabled(true);
        }
    }

    private void checkTheme() {
        if (lgwSettings.getTheme().equals("dark"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void onSetRegionIndex(int selection) {
        if (lgwSettings.getRegionIndex() != selection) {
            soundPool.play(SOUND_SHOT, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
            lgwSettings.setRegionIndex(selection);
            lgwSettings.save();
        }
        updateUiRegion();
    }
}