package com.commandus.lgw;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.MenuProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.commandus.lgw.databinding.ActivityMainBinding;
import com.commandus.serial.SerialPort;

import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
    implements ServiceConnection, LGWListener, RegionDialog.RegionSelectListener,
        MenuProvider
{
    private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    private LGWService service;
    private LgwSettings lgwSettings;
    private BroadcastReceiver broadcastReceiver;

    private Button buttonRegion;
    private SwitchCompat switchGateway;
    private TextView textStatusUSB;
    private TextView textStatusLGW;
    private TextView textCountReceive;
    private TextView textCountValue;
    private RecyclerView recyclerViewLog;
    private Button buttonDevices;
    private String gwId = "";
    private MenuItem menuItemGateway;

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((LGWService.LGWServiceBinder) binder).getService();
        service.attach(this);
        recyclerViewLog.setAdapter(service.gatewayEventAdapter);
        runOnUiThread(() -> {
            updateUiRegion();
            // reflect does USB gateway connected already
            if (service.isUsbConnected) {
                if (!SerialPort.hasDevice(this)) {
                    service.onDisconnectUsb();
                    pushMessage(getString(R.string.msg_usb_detached));
                }
            } else {
                service.checkIsUSBConnected();
            }
            reflectUSBConnected(service.isUsbConnected);
            reflectGatewayRunning(service.running);
            if (service.isUsbConnected) {
                // auto-start
                if (lgwSettings.getAutoStart()) {
                    reflectGatewayRunning(startLGW());
                }
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        recyclerViewLog.setAdapter(null);
        service = null;
        runOnUiThread(this::onUsbDisconnected);
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lgwSettings = LgwSettings.getSettings(this);

        com.commandus.lgw.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gwId = lgwSettings.getLastGatewayEUI();

        buttonRegion = binding.buttonRegion;
        buttonDevices = binding.buttonDevices;
        switchGateway = binding.switchGateway;
        textStatusUSB = binding.textStatusUSB;
        textStatusLGW = binding.textStatusLGW;
        textCountReceive = binding.textLGWReceiveCount;
        textCountValue = binding.textLGWValueCount;
        recyclerViewLog = binding.recyclerViewLog;

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

        addMenuProvider(this);

        buttonRegion.setOnClickListener(view -> selectRegion());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                doUSBAction(intent);
            }
        };

        startService(new Intent(this, LGWService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
        applySettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reload settings after SettingsActivity returns
        lgwSettings.load();
        // apply settings (if any)
        applySettings();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_gateway, menu);
        menuItemGateway = menu.findItem(R.id.action_gateway);
        updateUiGatewayId();
    }

    private void updateUiGatewayId() {
        if (menuItemGateway != null) {
            menuItemGateway.setTitle(gwId);
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_gateway:
                // It cause Fatal signal 5 (SIGTRAP), code 1 in tid 16364 (RenderThread)
                /*
                LgwHelper.copy2clipboard(this, getString(R.string.msg_gateway_identifier),
                        gwId, getString(R.string.msg_gateway_id_copied));
                 */
                return true;
            case R.id.action_preferences:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_payload:
                Intent intent2 = new Intent(MainActivity.this, PayloadActivity.class);
                startActivity(intent2);
                return true;
            case R.id.action_help:
                Intent intent3 = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent3);
                return true;
            case R.id.action_send_log_snapshot:
                shareLog();
                return true;
        }
        return false;
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
        if (service.running)
            return true;
        if (!isUSBConnected()) {
            pushMessage(getString(R.string.message_no_usb_connection_established));
            return false;
        }
        return service.startGateway(lgwSettings.getRegionIndex(), 0);
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

        int cnt = DeviceAddressProvider.count(this);
        if (cnt > 0)
            buttonDevices.setText(getString(R.string.label_button_device_count) + cnt);
        else
            buttonDevices.setText(R.string.label_button_devices);
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
                pushMessage("USB device access permission granted");
                connectUSB();
            } else {
                pushMessage("USB device access permission denied, quit..");
            }
        }
        if (ACTION_USB_ATTACHED.equals(action)) {
            if (SerialPort.hasDevice(this)) {
                pushMessage(getString(R.string.msg_usb_attached));
                connectUSB();
                // auto-start
                if (lgwSettings.getAutoStart()) {
                    reflectGatewayRunning(startLGW());
                }
            }
        }
        if (ACTION_USB_DETACHED.equals(action)) {
            pushMessage(getString(R.string.msg_usb_detached));
            disconnectUSB();
        }
    }
    private void pushMessage(String msg) {
        if (service == null)
            return;
        service.gatewayEventAdapter.push(msg);
        recyclerViewLog.smoothScrollToPosition(service.gatewayEventAdapter.logData.size() - 1);
    }
    @Override
    public void onStarted(String gatewayId, String regionName, int regionIndex) {
        // show gateway identifier
        if (!Objects.equals(gwId, gatewayId)) {
            gwId = gatewayId;
            lgwSettings.setLastGatewayEUI(gwId);
            lgwSettings.save();
            updateUiGatewayId();
        }

        pushMessage(getString(R.string.msg_running) + " " + regionName
            + ". " + getString(R.string.label_gateway) + " " + gatewayId);
        pushMessage(getString(R.string.msg_running_warning));
        reflectGatewayRunning(true);
    }

    private void reflectGatewayRunning(boolean on) {
        switchGateway.setChecked(on);
        textStatusLGW.setText(on ? R.string.label_running : R.string.label_stopped);
        switchGateway.setText(on ? R.string.gateway_on : R.string.gateway_off);
    }
    @Override
    public void onFinished(String message) {
        pushMessage(message);
        reflectGatewayRunning(false);
    }
    @Override
    public byte[] onRead(int bytes) {
        return new byte[0];
    }
    @Override
    public int onWrite(byte[] data) {
        return 0;
    }

    @Override
    public int onSetAttr(boolean blocking) {
        return 0;
    }

    @Override
    public LoraDeviceAddress onIdentityGet(String devAddr) {
        // not used
        return null;
    }

    @Override
    public LoraDeviceAddress onGetNetworkIdentity(String devEui) {
        // not used
        return null;
    }

    @Override
    public int onIdentitySize() {
        // not used
        return 0;
    }

    @Override
    public void onReceive(Payload value) {
        // refresh view
        recyclerViewLog.smoothScrollToPosition(service.gatewayEventAdapter.logData.size() - 1);
        textCountReceive.setText(Integer.toString(service.receiveCount));
    }

    @Override
    public void onValue(Payload value) {
        // refresh view
        recyclerViewLog.smoothScrollToPosition(service.gatewayEventAdapter.logData.size() - 1);
        textCountValue.setText(Integer.toString(service.valueCount));
    }

    @Override
    public void onInfo(int severity, String msg)
    {
        pushMessage(msg);
    }

    @Override
    public void onUsbConnected(boolean on) {
        pushMessage(getString(R.string.msg_connected));
        reflectUSBConnected(on);
    }

    @Override
    public void onUsbDisconnected() {
        pushMessage(getString(R.string.label_disconnected));
        reflectUSBConnected(false);
    }

    private void disconnectUSB() {
        if (service != null) {
            service.stopGateway();
            service.isUsbConnected = false;
        }
        reflectUSBConnected(false);
    }
    private boolean isUSBConnected() {
        if (service == null)
            return false;
        return service.isUsbConnected;
    }

    private void connectUSB() {
        if (service == null) {
            pushMessage(getString(R.string.msg_no_service));
            reflectUSBConnected(false);
            return;
        }
        if (service.isUsbConnected) {
            pushMessage(getString(R.string.msg_already_connected));
            reflectUSBConnected(true);
            return;
        }
        if (SerialPort.hasDevice(this)) {
            if (!service.connectSerialPort()) // permission not granted
                return;
        } else {
            pushMessage(getString(R.string.msg_unknown_usb_device));
        }
        reflectUSBConnected(service.isUsbConnected);
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

    /**
     * After settings has been changed apply settings
     */
    private void applySettings() {
        // do not turn off screen
        if (lgwSettings.getKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (lgwSettings.getTheme().equals(getString(R.string.theme_name_dark)))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // service
        if (service != null) {
            service.setContentProviderUri(lgwSettings.getContentProviderUri());
        }
    }

    @Override
    public void onSetRegionIndex(int selection) {
        if (lgwSettings.getRegionIndex() != selection) {
            lgwSettings.setRegionIndex(selection);
            lgwSettings.save();
            if (service != null) {
                if (service.running) {
                    stopLGW();
                    reflectGatewayRunning(startLGW());
                }
            }
        }
        updateUiRegion();
    }

    private void shareLog() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, LogHelper.snapshot());
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.msg_share_log)
                + " " + new Date());
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

}

