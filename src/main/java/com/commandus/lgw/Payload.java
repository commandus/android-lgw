package com.commandus.lgw;

import java.util.Date;

public class Payload {
    Date received;
    String devEmui;
    String devName;
    String hexPayload;
    int frequency;
    int rssi;
    float lsnr;

    public void reset() {
        received = new Date();
        devEmui = "";
        devName = "";
        hexPayload = "";
        frequency = 0;
        rssi = 0;
        lsnr = 0.0f;
    }

    public Payload() {
        reset();
    }

    public Payload(
        String payload
    ) {
        reset();
        this.hexPayload = payload;
    }

    public Payload(Payload p) {
        received = p.received;
        devEmui = p.devEmui;
        devName = p.devName;
        hexPayload = p.hexPayload;
        frequency = p.frequency;
        rssi = p.rssi;
        lsnr = p.lsnr;
    }

    @Override
    public String toString() {
        return "Payload {" +
            "\"received\": \"" + received.toString() +
            "\", \"devEmui\": \"" + devEmui +
            "\", \"devName\": \"" + devName +
            "\", \"hexPayload\": \"" + hexPayload +
            "\", \"frequency\": " + frequency +
            ", \"rssi\": \"" + Integer.toString(rssi) +
            ", \"lsnr\": \"" + Float.toString(lsnr) +
        '}';
    }
}
