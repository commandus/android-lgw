package com.commandus.lgw;


/**
 *   ABP
 *   {
 *     "addr": "xx..",
 *     "eui": "xx..:",
 *     "nwkSKey": "..",
 *     "appSKey": "..",
 *     "name": "dev01"
 *   },
 */
public class LoraDeviceAddress {
    String addr;
    DevEUI eui;
    KEY128 nwkSKey;
    KEY128 appSKey;
    String name;
}
