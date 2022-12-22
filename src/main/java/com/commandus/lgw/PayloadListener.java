package com.commandus.lgw;

public interface PayloadListener {
    void onValue(final Payload value);
    void onInfo(final String msg);
}
