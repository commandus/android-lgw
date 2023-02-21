package com.commandus.lgw;

public interface LoraWanGateway {
    /**
     * @return LoRaWAN gateway library version
     */
    public String getVersion();

    /**
     * @return Region settings names
     */
    public String[] getRegionNames();

    /**
     * Connect to events
     * @param logger
     */
    public void assignPayloadListener(LGWListener logger);

    /**
     * Start LoRaWAN gateway
     * @param regionIndex Index of region settings
     * @param gwId gateway identifier
     * @param verbosity 0- none, 7- debug
     * @return 0- success
     */
    public int startGateway(int regionIndex, String gwId, int verbosity);

    /**
     * Stop LoRaWAN gateway
     */
    public void stopGateway();

}
