package com.commandus.lgw;

import androidx.annotation.NonNull;

import java.util.Date;

public class Payload {
    Date received;
    String devEmui;
    String devName;
    public String hexPayload;
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

    public Payload(
        String hexPayload,
        int frequency,
        int rssi,
        float lsnr
    ) {
        this.received = new Date();
        this.devEmui = "";
        this.devName = "";
        this.hexPayload = "";
        this.frequency = frequency;
        this.rssi = rssi;
        this.lsnr = lsnr;
        this.hexPayload = hexPayload;
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

    @NonNull
    @Override
    public String toString() {
        return "Payload {" +
            "\"received\": \"" + received.toString() +
            "\", \"devEmui\": \"" + devEmui +
            "\", \"devName\": \"" + devName +
            "\", \"hexPayload\": \"" + hexPayload +
            "\", \"frequency\": " + frequency +
            ", \"rssi\": \"" + rssi +
            ", \"lsnr\": \"" + lsnr +
        '}';
    }
}
