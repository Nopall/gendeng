package com.softdev.weekimessenger.Services;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Nopal on 09/09/2017.
 */

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, GCMService.class);
        context.startService(myIntent);
    }
}
