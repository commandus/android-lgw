package com.commandus.lgw;

import android.app.Activity;
import android.util.JsonReader;

import com.commandus.gui.DeviceAddressProvider;

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
                List<LoraDeviceAddress> v = new ArrayList<>();

                try (JsonReader reader = new JsonReader(new InputStreamReader(urlConn.getInputStream()))) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        LoraDeviceAddress d = new LoraDeviceAddress();
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name = reader.nextName();
                            if (name.equals(DeviceAddressProvider.F_ADDRESS)) {
                                d.addr = reader.nextString();
                            } else if (name.equals(DeviceAddressProvider.F_EUI)) {
                                d.eui = new DevEUI(reader.nextString());
                            } else if (name.equals(DeviceAddressProvider.F_NWKSKEY)) {
                                d.nwkSKey = new KEY128(reader.nextString());
                            } else if (name.equals(DeviceAddressProvider.F_APPSKEY)) {
                                d.appSKey = new KEY128(reader.nextString());
                            } else if (name.equals(DeviceAddressProvider.F_NAME)) {
                                d.name = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        v.add(d);
                    }
                    reader.endArray();
                    if (retVal != null) {
                        // context.runOnUiThread(() -> retVal.onDone(0, v));
                        retVal.onDone(0, v);
                    }
                }
            } catch (Exception ignored) {
            }
        });
        t.start();
    }
}