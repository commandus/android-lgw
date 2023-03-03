package com.commandus.lgw;

public interface AddressListResult {
    void onDone(int status);
    void onAddress(LoraDeviceAddress value);
    void onError(String message);
}
