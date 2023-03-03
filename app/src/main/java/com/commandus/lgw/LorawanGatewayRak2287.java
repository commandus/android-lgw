package com.commandus.lgw;

public class LorawanGatewayRak2287 implements LoraWanGateway {
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
    private native String[] regionNames();

    /**
     * Connect to events
     * @param logger gateway listener
     */
    private native void setPayloadListener(LGWListener logger);

    /**
     * Start LoRaWAN gateway
     * @param regionIndex Index of region settings
     * @param gwId gateway identifier
     * @param verbosity 0- none, 7- debug
     * @return 0- success
     */
    private native int start(int regionIndex, String gwId, int verbosity);

    /**
     * Stop LoRaWAN gateway
     */
    private native void stop();

    @Override
    public String getVersion() {
        return version();
    }

    @Override
    public String[] getRegionNames() {
        return regionNames();
    }

    @Override
    public void assignPayloadListener(LGWListener logger) {
        setPayloadListener(logger);
    }

    @Override
    public int startGateway(int regionIndex, String gwId, int verbosity) {
        return start(regionIndex, gwId, verbosity);
    }

    @Override
    public void stopGateway() {
        stop();
    }
}
