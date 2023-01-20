package com.commandus.lgw;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class LgwHelper {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void log2file(String message) {
        if (true) {
            try {
                String path = "/storage/emulated/0/Android/data/com.commandus.lgw/files/Documents/lgw_com.log";
                File docs = new File(path);
                FileOutputStream output = new FileOutputStream(docs.getAbsoluteFile(), true);
                output.write((new Date().toString() + ": " + message + "\r\n").getBytes());
                output.close();
            } catch (Exception ignored) {
            }
        }
    }


}
