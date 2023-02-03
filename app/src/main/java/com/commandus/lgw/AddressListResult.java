package com.commandus.lgw;

import java.util.List;

public interface AddressListResult {
    public void onDone(int status, List<LoraDeviceAddress> values);
}
