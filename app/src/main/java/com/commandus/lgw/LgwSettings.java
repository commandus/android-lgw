package com.commandus.lgw;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class LgwSettings {
    public static final int USB_VENDOR_ID = 0x0483; // 0x16d0;
    public static final int USB_PRODUCT_ID = 0x5740; // 0x087e;
    public static final String APPLICATION_ID = "com.commandus.loraGatewayListener";
    public static final String INTENT_ACTION_GRANT_USB = APPLICATION_ID + ".GRANT_USB";
    public static final String INTENT_ACTION_DISCONNECT = APPLICATION_ID + ".Disconnect";
    private static final String PREF_THEME = "theme";
    private static final String PREF_REGION_INDEX = "region_index";

    private static LgwSettings mLgwSettings = null;
    private final Context mContext;

    private String mTheme; // light|dark
    private int mRegionIndex;
    private int mVerbosity = 7;

    public String getTheme() {
        return mTheme;
    }

    public void load() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mTheme = prefs.getString(PREF_THEME, "dark");
        mRegionIndex = prefs.getInt(PREF_REGION_INDEX, 0);
    }

    public LgwSettings(Context context) {
        mContext = context;
        load();
    }

    public void save() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(PREF_THEME, mTheme);
        editor.putInt(PREF_REGION_INDEX, mRegionIndex);

        editor.apply();
    }

    public synchronized static LgwSettings getSettings(Context context) {
        if (mLgwSettings == null) {
            mLgwSettings = new LgwSettings(context);
        }
        return mLgwSettings;
    }

    public int getRegionIndex() {
        return mRegionIndex;
    }

    public void setRegionIndex(int value) {
        mRegionIndex = value;
    }

    public boolean isFileLog() {
        return true;
    }

    public int getVerbosity() {
        return mVerbosity;
    }
}
