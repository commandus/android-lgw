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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
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

    private int mReadCount;
    private int mWriteCount;

    private SoundPool soundPool;
    private LGWService service;
    private LgwSettings lgwSettings;
    private BroadcastReceiver broadcastReceiver;
    private PayloadAdapter payloadAdapter;
    private DeviceAddresses deviceAddresses;

    private Button buttonRegion;
    private SwitchCompat switchGateway;
    private TextView textStatusUSB;
    private TextView textCountRead;
    private TextView textCountWrite;
    private Button buttonDevices;

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((LGWService.LGWServiceBinder) binder).getService();
        service.attach(this);
        mReadCount = 0;
        mWriteCount = 0;

        runOnUiThread(() -> {
            updateUiRegion();
            @SuppressLint("HardwareIds") String gwId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            setTitle(getString(R.string.app_name) + " " + gwId);
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        runOnUiThread(() -> {
            onDisconnected();
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
        switchGateway = binding.switchGateway;
        textStatusUSB = binding.textStatusUSB;
        textCountRead = binding.textLGWReadCount;
        textCountWrite = binding.textLGWWriteCount;

        // TextView textStatusLGW = binding.textStatusLGW;
        RecyclerView recyclerViewLog = binding.recyclerViewLog;

        deviceAddresses = new DeviceAddresses();
        int cnt = 0;
        if (cnt > 0)
            buttonDevices.setText(getString(R.string.label_button_device_count) + cnt);
        else
            buttonDevices.setText(R.string.label_button_devices);

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

        payloadAdapter = new PayloadAdapter(recyclerViewLog);
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
        if (service == null) {
            onInfo("startLGW: no service");
            return false;
        }

        if (!isUSBConnected()) {
            onInfo("startLGW: no USB connection");
            if (FTDI.hasDevice(MainActivity.this)) {
                connectUSB();
            }
        }
        if (!isUSBConnected()) {
            onInfo("startLGW: no USB connection established");
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
    public void onStarted(String gatewayId, String regionName, int regionIndex) {
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
    public byte[] onRead() {
        incReads();
        return new byte[0];
    }

    @Override
    public int onWrite(byte[] data) {
        incWrites();
        return 0;
    }

    @Override
    public int onSetAttr(boolean blocking) {
        return 0;
    }

    @Override
    public void onValue(Payload value) {
        payloadAdapter.push(value.hexPayload);
    }

    @Override
    public void onInfo(String msg)
    {
        Log.d("main", msg);
        payloadAdapter.push(msg);
    }

    @Override
    public void onConnected(boolean on) {
        setUIUSBConnected(on);
    }

    @Override
    public void onDisconnected() {
        setUIUSBConnected(false);
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
            onInfo("No service");
            setUIUSBConnected(false);
            return;
        }
        if (service.connected) {
            onInfo("Already connected");
            setUIUSBConnected(true);
            return;
        }
        if (FTDI.hasDevice(this)) {
            if (!service.connectSerialPort()) // permission not granted
                return;
        } else {
            onInfo("Unknown USB device");
        }
        setUIUSBConnected(service.connected);
        if (service.connected)
            soundPool.play(SOUND_ON, 1.0f, 1.0f, SOUND_PRIORITY_1, 0, 1.0f);
        if (service.connected)
            onInfo("Successfully connected");
        else
            onInfo("Not connected");
    }

    private void setUIUSBConnected(boolean connected) {
        if (connected) {
            textStatusUSB.setText(getString(R.string.label_connected));
            switchGateway.setChecked(false);
            switchGateway.setEnabled(true);
        } else {
            textStatusUSB.setText(getString(R.string.label_disconnected));
            switchGateway.setChecked(false);
            switchGateway.setEnabled(false);
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

    private void incReads() {
        mReadCount++;
        textCountRead.setText(Integer.toString(mReadCount));
    }

    private void incWrites() {
        mWriteCount++;
        textCountWrite.setText(Integer.toString(mWriteCount));
    }

}