package com.commandus.lgw;


import android.content.ContentValues;
import android.text.Editable;

import com.commandus.gui.DeviceAddressProvider;

/**
 *   ABP
 */
public class LoraDeviceAddress {
    public long id;
    public String addr;
    public DevEUI eui;
    public KEY128 nwkSKey;
    public KEY128 appSKey;
    public String name;

    public LoraDeviceAddress(long id, String addr, String eui, String nwkSKey, String appSKay, String name ) {
        this.id = id;
        this.addr = addr;
        this.eui = new DevEUI(eui);
        this.nwkSKey = new KEY128(nwkSKey);
        this.appSKey = new KEY128(appSKay);
        this.name = name;
    }

    public LoraDeviceAddress() {
        this.id = 0;
    }

    public ContentValues getContentValues() {
        ContentValues r = new ContentValues();
        r.put(DeviceAddressProvider.FN_ADDRESS, addr);
        r.put(DeviceAddressProvider.FN_EUI, eui.toString());
        r.put(DeviceAddressProvider.FN_NWKSKEY, nwkSKey.toString());
        r.put(DeviceAddressProvider.FN_ADDRESS, appSKey.toString());
        r.put(DeviceAddressProvider.FN_NAME, name);
        return r;
    }
}
