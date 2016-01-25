package com.zh.steve.grabbing.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zh.steve.grabbing.Constants;
import com.zh.steve.grabbing.UDPListenerService;

/**
 * Created by Steve Zhang
 * 1/3/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent mIntent = new Intent(context, UDPListenerService.class);
            context.startService(mIntent);
            Log.d(TAG, "Start service by boot completed");
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Intent mIntent = new Intent(context, UDPListenerService.class);
            context.startService(mIntent);
            Log.d(TAG, "Start service by user present");
        } else if (intent.getAction().equals(Constants.ACTION_DESTORY_SERVICE)) {
            Intent mIntent = new Intent(context, UDPListenerService.class);
            context.startService(mIntent);
            Log.d(TAG, "Start service by udp listener service destory");
        }
    }
}

