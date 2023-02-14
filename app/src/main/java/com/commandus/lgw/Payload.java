package com.commandus.lgw;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import java.util.Date;

public class Payload {
    private static final String[] FIELD_NAMES = {
        "id",
        "received",
        "eui",
        "name",
        "payload",
        "frequency",
        "rssi",
        "lsnr"
    };

    long id;
    Date received;
    String devEui;
    String devName;
    public String hexPayload;
    int frequency;
    int rssi;
    float lsnr;

    public void reset() {
        id = 0L;
        received = new Date();
        devEui = "";
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
        id = 0L;
        this.received = new Date();
        this.devEui = "";
        this.devName = "";
        this.hexPayload = "";
        this.frequency = frequency;
        this.rssi = rssi;
        this.lsnr = lsnr;
        this.hexPayload = hexPayload;
    }

    public Payload(Payload p) {
        id = p.id;
        received = p.received;
        devEui = p.devEui;
        devName = p.devName;
        hexPayload = p.hexPayload;
        frequency = p.frequency;
        rssi = p.rssi;
        lsnr = p.lsnr;
    }

    public Payload(
        long id,
        long receeived,
        String devEmui,
        String devName,
        String hexPayload,
        int frequency,
        int rssi,
        float lsnr
    ) {
        this.id = id;
        this.received = new Date(receeived * 1000L);
        this.devEui = devEmui;
        this.devName = devName;
        this.hexPayload = hexPayload;
        this.frequency = frequency;
        this.rssi = rssi;
        this.lsnr = lsnr;
        this.hexPayload = hexPayload;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" +
            "\"" + FIELD_NAMES[0] + "\": " + id +
            ", \"" + FIELD_NAMES[1] + "\": \"" + received.toString() +
            "\", \"" + FIELD_NAMES[2] + "\": \"" + devEui +
            "\", \"" + FIELD_NAMES[3] + "\": \"" + devName +
            "\", \"" + FIELD_NAMES[4] + "\": \"" + hexPayload +
            "\", \"" + FIELD_NAMES[5] + "\": " + frequency +
            ", \"" + FIELD_NAMES[6] + "\": \"" + rssi +
            ", \"" + FIELD_NAMES[7] + "\": \"" + lsnr +
        "}";
    }

    public ContentValues getContentValues() {
        ContentValues r = new ContentValues();
        if (id > 0)
            r.put(FIELD_NAMES[0], id);
        r.put(FIELD_NAMES[1], received.getTime() / 1000L);
        r.put(FIELD_NAMES[2], devEui);
        r.put(FIELD_NAMES[3], devName);
        r.put(FIELD_NAMES[4], hexPayload);
        r.put(FIELD_NAMES[5], frequency);
        r.put(FIELD_NAMES[6], rssi);
        r.put(FIELD_NAMES[7], lsnr);
        return r;
    }
}
