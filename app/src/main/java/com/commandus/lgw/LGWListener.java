package com.commandus.lgw;

public interface LGWListener {
    void onReceive(final Payload value);
    void onValue(final Payload value);
    void onInfo(final String msg);
    void onConnected(final boolean on);
    void onDisconnected();
    void onStarted(String gatewayId, String regionName, int regionIndex);
    void onFinished(String message);
    byte[] onRead(int bytes);
    int onWrite(byte[] data);
    int onSetAttr(
            boolean blocking
    );
}
