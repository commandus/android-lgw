package com.commandus.lgw;

public interface SerialIO {
    byte[] onRead(int bytes);
    int onWrite(byte[] data);
    int onSetAttr(
        boolean blocking
    );
}
