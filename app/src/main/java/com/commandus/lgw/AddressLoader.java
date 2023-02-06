package com.commandus.lgw;

import android.util.JsonReader;
import android.util.Log;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class AddressLoader {
    private AddressListResult mResult;

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
                            if (name.equals(DeviceAddressProvider.FN_ADDR)) {
                                d.addr = reader.nextString();
                            } else if (name.equals(DeviceAddressProvider.FN_DEVEUI)) {
                                d.devEui = new DevEUI(reader.nextString());
                            } else if (name.equals(DeviceAddressProvider.FN_NWKSKEY)) {
                                d.nwkSKey = new KEY128(reader.nextString());
                            } else if (name.equals(DeviceAddressProvider.FN_APPSKEY)) {
                                d.appSKey = new KEY128(reader.nextString());
                            } else if (name.equals(DeviceAddressProvider.FN_NAME)) {
                                d.name = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        if (!d.nwkSKey.empty() && !d.appSKey.empty())
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