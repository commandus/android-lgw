package com.commandus.lgw;

public interface LGWListener {
    void onReceive(final Payload value);
    void onValue(final Payload value);
    void onInfo(int severity, final String msg);
    void onUsbConnected(final boolean on);
    void onUsbDisconnected();
    void onStarted(String gatewayId, String regionName, int regionIndex);
    void onFinished(String message);
    byte[] onRead(int bytes);
    int onWrite(byte[] data);
    int onSetAttr(
            boolean blocking
    );

    /**
     * return device identifier by the address
     * @param devAddr end-device address
     * @return null if not found
     */
    LoraDeviceAddress onIdentityGet(String devAddr);
    LoraDeviceAddress onGetNetworkIdentity(String devEui);
    int onIdentitySize();
}
