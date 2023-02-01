package com.commandus.lgw;

import androidx.annotation.NonNull;

public class GatewayEvent {
    public String message;
    public Payload rawPayload;
    public Payload payload;

    public boolean hasPayload() {
        return (payload != null);
    }

    public boolean hasRawPayload() {
        return (rawPayload != null);
    }

    public boolean hasMessage() {
        return (message != null);
    }

    @NonNull
    @Override
    public String toString() {
        if (hasMessage()) {
            return message;
        }
        if (hasRawPayload()) {
            StringBuilder b = new StringBuilder();
            b
                .append(rawPayload.frequency)
                .append("Hz ")
                .append(rawPayload.rssi)
                .append("dBi ")
                .append(rawPayload.lsnr)
                .append("dB ")
                .append(rawPayload.hexPayload);
            return b.toString();
        }
        if (hasPayload()) {
            StringBuilder b = new StringBuilder();
            b
                .append(payload.frequency)
                .append("Hz ")
                .append(payload.rssi)
                .append("dBi ")
                .append(payload.lsnr)
                .append("dB ")
                .append(payload.hexPayload);
            return b.toString();
        }
        return "";
    }
}
