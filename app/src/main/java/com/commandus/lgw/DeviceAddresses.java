package com.commandus.lgw;

import android.content.Context;

public class DeviceAddresses {
    private final DeviceAddressProvider mDeviceProvider;

    public DeviceAddresses() {
        mDeviceProvider = new DeviceAddressProvider();
    }

    public int count() {
        return mDeviceProvider.count();
    }
}
