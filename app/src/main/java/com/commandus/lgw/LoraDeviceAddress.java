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
        if (id != 0L)
            r.put(DeviceAddressProvider.FN_ID, id);
        r.put(DeviceAddressProvider.FN_ADDR, addr);
        r.put(DeviceAddressProvider.FN_DEVEUI, devEui.toString());
        r.put(DeviceAddressProvider.FN_NWKSKEY, nwkSKey.toString());
        r.put(DeviceAddressProvider.FN_APPSKEY, appSKey.toString());
        r.put(DeviceAddressProvider.FN_NAME, name);
        return r;
    }

    public String toJson() {
        StringBuilder b = new StringBuilder();
        b.append("{\"").append(DeviceAddressProvider.FN_ADDR).append("\": \"").append(addr)
            .append("\", \"").append(DeviceAddressProvider.FN_DEVEUI).append("\": \"").append(devEui.toString())
            .append("\", \"").append(DeviceAddressProvider.FN_NWKSKEY).append("\": \"").append(nwkSKey.toString())
            .append("\", \"").append(DeviceAddressProvider.FN_APPSKEY).append("\": \"").append(appSKey.toString())
            .append("\", \"").append(DeviceAddressProvider.FN_NAME).append("\": \"").append(name)
            .append("\"");
        if (id != 0L)
            b.append(", \"").append(DeviceAddressProvider.FN_ID).append("\": ").append(id);
        b.append("}");
        return b.toString();
    }
}
