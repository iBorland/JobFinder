package com.iborland.jobfinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by iBorland on 18.04.2016.
 */
public class RebootService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MessageService.class));
    }
}
