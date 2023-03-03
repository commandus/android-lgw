package com.commandus.lgw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receive android.intent.action.BOOT_COMPLETED broadcast and start up
 */
public class ReceiverBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LgwSettings settings = LgwSettings.getSettings(context);
        boolean autoStart = settings.getStartAtBoot();
        Log.d(LogHelper.TAG, "device rebooted, auto-start: " + autoStart);
        if (autoStart) {
            Intent intentA = new Intent(context, MainActivity.class);
            intentA.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentA);
            Log.d(LogHelper.TAG, "Main activity started");
        }
    }
}
