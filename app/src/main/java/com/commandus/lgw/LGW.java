package com.commandus.lgw;

public class LGW {
    // Used to load the 'loraGatewayListener' library on application startup.
    static {
        System.loadLibrary("loragw");
    }

    /**
     * @return LoRaWAN gateway library version
     */
    public native String version();

    /**
     * @return Region settings names
     */
    public native String[] regionNames();

    /**
     * Connect to events
     * @param logger
     */
    public native void setPayloadListener(LGWListener logger);

    /**
     * Start LoRaWAN gateway
     * @param regionIndex Index of region settings
     * @param gwId gateway identifier
     * @param verbosity 0- none, 7- debug
     * @return 0- success
     */
    public native int start(int regionIndex, String gwId, int verbosity);

    /**
     * Stop LoRaWAN gateway
     */
    public native void stop();
}

