package com.commandus.lgw;

public class LGW {
    // Used to load the 'lgw' library on application startup.
    static {
        System.loadLibrary("loragw");
    }
    public native String version();
    public native void setLog(LogIntf logger);
    public native int start(boolean connected, int fd);
    public native void stop();
}

