package com.commandus.lgw;

public class LorawanGatewayFake implements LoraWanGateway {
    // Used to load the 'loraGatewayListener' library on application startup.
    static {
        System.loadLibrary("loragw");
    }
    private static final String[] mRegionNames = new String[] {
        "AS915-921",
        "AS915-928",
        "AS917-920",
        "AS920-923",
        "AU915-928",
        "CN470-510",
        "EU433",
        "EU863-870",
        "IN865-867",
        "KR920-923",
        "RU864-870",
        "US902-928"
};

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String[] getRegionNames() {
        return mRegionNames;
    }

    private native void setPayloadListener(LGWListener logger);

    @Override
    public void assignPayloadListener(LGWListener logger) {
        setPayloadListener(logger);
   }

    private native int start(int regionIndex, String gwId, int verbosity);

    @Override
    public int startGateway(int regionIndex, String gwId, int verbosity) {
        return start(regionIndex, gwId, verbosity);
    }

    /**
     * Stop LoRaWAN gateway
     */
    private native void stop();
    @Override
    public void stopGateway() {
        stop();
    }
}
