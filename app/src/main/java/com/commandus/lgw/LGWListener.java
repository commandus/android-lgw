package com.commandus.lgw;

public interface LGWListener {
    void onValue(final Payload value);
    void onInfo(final String msg);
    void onConnected(final boolean on);
    void onDisconnected();
    void onStarted(int fd, String gatewayId, String regionName, int regionIndex);
    void onFinished(String message);
    byte[] onRead();
    int onWrite(byte[] data);
}
