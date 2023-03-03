package com.commandus.lgw;

import android.util.JsonReader;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class AddressLoader {

    public AddressLoader(String url, AddressListResult retVal) {
        Thread t = new Thread(() -> {
            try {
                URL u = new URL(url);
                URLConnection urlConn = u.openConnection();
                try (JsonReader reader = new JsonReader(new InputStreamReader(urlConn.getInputStream()))) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        LoraDeviceAddress d = new LoraDeviceAddress();
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name = reader.nextName();
                            switch (name) {
                                case DeviceAddressProvider.FN_ADDR:
                                    d.addr = reader.nextString();
                                    break;
                                case DeviceAddressProvider.FN_DEVEUI:
                                    d.devEui = new DevEUI(reader.nextString());
                                    break;
                                case DeviceAddressProvider.FN_NWKSKEY:
                                    d.nwkSKey = new KEY128(reader.nextString());
                                    break;
                                case DeviceAddressProvider.FN_APPSKEY:
                                    d.appSKey = new KEY128(reader.nextString());
                                    break;
                                case DeviceAddressProvider.FN_NAME:
                                    d.name = reader.nextString();
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                        if (d.nwkSKey != null && !d.nwkSKey.empty()
                                && d.appSKey != null && !d.appSKey.empty())
                            retVal.onAddress(d);
                    }
                    reader.endArray();
                    if (retVal != null) {
                        // context.runOnUiThread(() -> retVal.onDone(0, v));
                        retVal.onDone(0);
                    }
                }
            } catch (Exception e) {
                if (retVal != null) {
                    retVal.onError(e.getLocalizedMessage());
                }
            }
        });
        t.start();
    }
}