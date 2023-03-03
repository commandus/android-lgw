package com.commandus.lgw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogHelper {
    public static final String TAG = "algw";

    public static String snapshot() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");   //  algw:V *:E
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line).append("\r\n");
            }
            return log.toString();
        }
        catch (IOException ignored) {
        }
        return "";
    }
}
