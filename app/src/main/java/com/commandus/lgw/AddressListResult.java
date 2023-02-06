package com.commandus.lgw;

import java.util.List;

public interface AddressListResult {
    public void onDone(int status);
    public void onAddress(LoraDeviceAddress value);

    void onError(String message);
}
