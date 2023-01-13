package com.commandus.lgw;

public class LGW {
    // Used to load the 'loraGatewayListener' library on application startup.
    static {
        System.loadLibrary("loragw");
    }
    public native String version();
    public native String[] regionNames();
    public native void setPayloadListener(LGWListener logger);
    public native int start(int fd, int regionIdx, String gwId);
    public native void stop();
}

