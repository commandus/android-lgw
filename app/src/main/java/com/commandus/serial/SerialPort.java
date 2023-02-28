package com.commandus.serial;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;

import com.commandus.lgw.LgwSettings;
import com.commandus.lgw.R;
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
            reason = context.getString(R.string.msg_unknown_usb_device);
            return null;
        }
        if (!manager.hasPermission(d)) {
            int flags = PendingIntent.FLAG_MUTABLE;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(LgwSettings.INTENT_ACTION_GRANT_USB), flags);

            manager.requestPermission(d, usbPermissionIntent);
            reason = context.getString(R.string.msg_usb_device_access_denied);
            return null;
        }

        UsbDeviceConnection connection = manager.openDevice(d);

        if (connection == null) {
            reason = context.getString(R.string.msg_usb_device_open_device) + " "
                    + Integer.toHexString(d.getVendorId()) + ":" + Integer.toHexString(d.getProductId()) + " "
                    + d.getProductName() + " "
                    + d.getManufacturerName() + " " + d.getDeviceName();
            return null;
        }
        if (driver == null) {
            reason = context.getString(R.string.msg_usb_device_no_driver);;
            return null;
        }
        if (driver.getPorts().size() == 0) {
            reason = context.getString(R.string.msg_usb_device_no_ports);;
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
            reason = e.getMessage();
            return null;
        }
        if (success)
            return new SerialSocket(context, connection, port);
        else {
            reason = context.getString(R.string.msg_open_port);;
            return null;
        }
    }

    public static void close(SerialSocket socket) {
        if (socket != null) {
            socket.disconnect();
        }
    }
}
