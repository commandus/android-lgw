package com.commandus.serial;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;

import com.commandus.lgw.LgwSettings;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class SerialPort {

    public static String reason;

    public static boolean hasDevice(Context context) {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        for (UsbSerialDriver v : availableDrivers) {
            UsbDevice d = v.getDevice();
            if (d.getVendorId() == LgwSettings.USB_VENDOR_ID && d.getProductId() == LgwSettings.USB_PRODUCT_ID) {
                return true;
            }
        }
        return false;
    }

    public static SerialSocket open(Context context) {
        reason = "";
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        UsbDevice d = null;
        UsbSerialDriver driver = null;
        for (UsbSerialDriver v : availableDrivers) {
            d = v.getDevice();
            if (d.getVendorId() == LgwSettings.USB_VENDOR_ID && d.getProductId() == LgwSettings.USB_PRODUCT_ID) {
                driver = v;
                break;
            }
        }
        if (d == null) {
            reason = "unknown USB device";
            return null;
        }
        if (!manager.hasPermission(d)) {
            int flags;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                flags = PendingIntent.FLAG_IMMUTABLE;
            else
                flags = 0;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(LgwSettings.INTENT_ACTION_GRANT_USB),
                    flags);
            manager.requestPermission(d, usbPermissionIntent);
            reason = "UDB device access denied";
            return null;
        }

        UsbDeviceConnection connection = manager.openDevice(d);

        if (connection == null) {
            reason = "open device "
                    + Integer.toHexString(d.getVendorId()) + ":" + Integer.toHexString(d.getProductId()) + " "
                    + d.getProductName() + " "
                    + d.getManufacturerName() + " " + d.getDeviceName();
            return null;
        }
        if (driver == null) {
            reason = "no driver";
            return null;
        }
        if (driver.getPorts().size() == 0) {
            reason = "no ports";
            return null;
        }

        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        boolean success;
        try {
            port.open(connection);
            // port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            success = true;
        } catch (IOException e) {
            reason = e.getMessage();
            return null;
        } catch (Exception e) {
            reason = "fatal " + e.getMessage();
            return null;
        }
        if (success)
            return new SerialSocket(context, connection, port);
        else {
            reason = "open port";
            return null;
        }
    }

    public static void close(SerialSocket socket) {
        if (socket != null) {
            socket.disconnect();
        }
    }
}
