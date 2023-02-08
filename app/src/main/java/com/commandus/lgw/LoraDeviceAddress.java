package com.commandus.lgw;


import android.content.ContentValues;

/**
 *   ABP
 */
public class LoraDeviceAddress {
    public long id;
    public String addr;
    public DevEUI devEui;
    public KEY128 nwkSKey;
    public KEY128 appSKey;
    public String name;

    public LoraDeviceAddress(long id, String addr, String devEui, String nwkSKey, String appSKey, String name) {
        this.id = id;
        this.addr = addr;
        this.devEui = new DevEUI(devEui);
        this.nwkSKey = new KEY128(nwkSKey);
        this.appSKey = new KEY128(appSKey);
        this.name = name;
    }

    public LoraDeviceAddress() {
        this.id = 0;
    }

    public ContentValues getContentValues() {
        ContentValues r = new ContentValues();
        r.put(DeviceAddressProvider.FN_ADDR, addr);
        r.put(DeviceAddressProvider.FN_DEVEUI, devEui.toString());
        r.put(DeviceAddressProvider.FN_NWKSKEY, nwkSKey.toString());
        r.put(DeviceAddressProvider.FN_APPSKEY, appSKey.toString());
        r.put(DeviceAddressProvider.FN_NAME, name);
        return r;
    }
}
