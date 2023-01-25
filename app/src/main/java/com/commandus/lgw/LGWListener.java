package com.commandus.lgw;

public interface LGWListener {
    void onValue(final Payload value);
    void onInfo(final String msg);
    void onConnected(final boolean on);
    void onDisconnected();
    void onStarted(String gatewayId, String regionName, int regionIndex);
    void onFinished(String message);
}
