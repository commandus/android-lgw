package com.commandus.ftdi;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.commandus.lgw.Settings;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class FTDI {

    public static boolean hasDevice(Context context) {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        for (UsbSerialDriver v : availableDrivers) {
            UsbDevice d = v.getDevice();
            if (d.getVendorId() == Settings.USB_VENDOR_ID && d.getProductId() == Settings.USB_PRODUCT_ID) {
                return true;
            }
        }
        return false;
    }

    public static UsbSerialPort open(Context context) {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        UsbDevice d = null;
        UsbSerialDriver driver = null;
        for (UsbSerialDriver v : availableDrivers) {
             d = v.getDevice();
            if (d.getVendorId() == Settings.USB_VENDOR_ID && d.getProductId() == Settings.USB_PRODUCT_ID) {
                driver = v;
                break;
            }
        }
        if (d == null) {
            return null;
        }
        UsbDeviceConnection connection = manager.openDevice(d);
        if (connection == null) {
            return null;
        }
        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        boolean success = false;
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            success = true;
        } catch (IOException e) {
        }
        if (success)
            return port;
        else {
            return null;
        }
    }

    public static void close(UsbSerialPort port) {
        if (port != null) {
            try {
                port.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
