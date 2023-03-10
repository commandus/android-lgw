package com.commandus.lgw;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LgwSettings {
    public static final int USB_VENDOR_ID = 0x0483; // 0x16d0;
    public static final int USB_PRODUCT_ID = 0x5740; // 0x087e;
    public static final String APPLICATION_ID = "com.commandus.loraGatewayListener";
    public static final String INTENT_ACTION_GRANT_USB = APPLICATION_ID + ".GRANT_USB";
    public static final String INTENT_ACTION_DISCONNECT = APPLICATION_ID + ".Disconnect";
    private static final String PREF_THEME = "theme";
    private static final String PREF_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String PREF_START_AT_BOOT = "start_at_boot";
    private static final String PREF_CONTENT_PROVIDER_URI = "content_provider";
    private static final String PREF_REGION_INDEX = "region_index";
    private static final String PREF_LOAD_LAST_URI = "load_last_uri";
    private static final String PREF_SHARE_LAST_URI = "share_last_uri";
    private static final String PREF_AUTO_START = "auto_start";
    private static final String PREF_LAST_GATEWAY_EUI = "last_gateway_eui";

    private static final String DEF_CONTENT_PROVIDER_URI = "content://lora.data/payload";
    private static LgwSettings mLgwSettings = null;
    private final Context mContext;
    private String mTheme; // light|dark
    private int mRegionIndex;
    private String mLoadLastUri;
    private String mShareLastUri;
    private String mContentProviderUri;
    private boolean mKeepScreenOn;
    private boolean mStartAtBoot;
    private boolean mAutoStart;
    private String mLastGatewayEUI;

    public String getTheme() {
        return mTheme;
    }
    public boolean getKeepScreenOn() {
        return mKeepScreenOn;
    }
    public boolean getStartAtBoot() {
        return mStartAtBoot;
    }
    public boolean getAutoStart() {
        return mAutoStart;
    }
    public String getContentProviderUri() {
        return mContentProviderUri;
    }
    public String getLastGatewayEUI() {
        return mLastGatewayEUI;
    }
    public void setLastGatewayEUI(String value) {
        mLastGatewayEUI = value;
    }

    /**
     * Load settings
     */
    public void load() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mTheme = prefs.getString(PREF_THEME, mContext.getString(R.string.theme_name_bright));
        mKeepScreenOn = prefs.getBoolean(PREF_KEEP_SCREEN_ON, false);
        mStartAtBoot = prefs.getBoolean(PREF_START_AT_BOOT, false);
        mAutoStart = prefs.getBoolean(PREF_AUTO_START, false);
        mContentProviderUri = prefs.getString(PREF_CONTENT_PROVIDER_URI, DEF_CONTENT_PROVIDER_URI);
        mRegionIndex = prefs.getInt(PREF_REGION_INDEX, 0);
        mLoadLastUri = prefs.getString(PREF_LOAD_LAST_URI, "");
        mShareLastUri = prefs.getString(PREF_LOAD_LAST_URI, "");
        mLastGatewayEUI = prefs.getString(PREF_LAST_GATEWAY_EUI, "");
        if (invalidate())
            save();
    }

    public LgwSettings(Context context) {
        mContext = context;
        load();
    }

    /**
     * Save settings
     */
    public void save() {
        invalidate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(PREF_THEME, mTheme);
        editor.putBoolean(PREF_KEEP_SCREEN_ON, mKeepScreenOn);
        editor.putBoolean(PREF_START_AT_BOOT, mStartAtBoot);
        editor.putBoolean(PREF_AUTO_START, mAutoStart);
        editor.putString(PREF_CONTENT_PROVIDER_URI, mContentProviderUri);
        editor.putInt(PREF_REGION_INDEX, mRegionIndex);
        editor.putString(PREF_LOAD_LAST_URI, mLoadLastUri);
        editor.putString(PREF_SHARE_LAST_URI, mShareLastUri);
        editor.putString(PREF_LAST_GATEWAY_EUI, mLastGatewayEUI);

        editor.apply();
    }

    /**
     * Check settings and fix invalid values to default
     * @return true if settings fixed (it means need to reload or save)
     */
    private boolean invalidate() {
        boolean r = false;
        if ((!mTheme.equals(mContext.getString(R.string.theme_name_dark)))) {
            r = mTheme.equals(mContext.getString(R.string.theme_name_bright));
            mTheme = mContext.getString(R.string.theme_name_bright);
        }
        if (mContentProviderUri.isEmpty()) {
            mContentProviderUri = DEF_CONTENT_PROVIDER_URI;
            r = true;
        }
        if (mRegionIndex < 0) {
            mRegionIndex = 0;
            r = true;
        }
        return r;
    }

    /**
     * Singleton
     * @param context application context
     * @return settings
     */
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
    public String getLoadLastUri() {
        return mLoadLastUri;
    }
    public void setLoadLastUri(String value) {
        mLoadLastUri = value;
    }
    public String getShareLastUri() {
        return mShareLastUri;
    }
    public void setSaveLastUri(String value) {
        mShareLastUri = value;
    }

}
