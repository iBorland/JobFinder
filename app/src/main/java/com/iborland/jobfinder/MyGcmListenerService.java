package com.iborland.jobfinder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by iBorland on 02.05.2016.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        int type = Integer.parseInt(data.getString("type"));
        int sender_id = Integer.parseInt(data.getString("sender_id"));
        String sender_name = data.getString("sender_name");
        String text = data.getString("text");

        Log.e(TAG, "Message: " + message + " type = " + type + " sender " + sender_id);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }
        sendNotification(sender_name);

        Intent sms = new Intent(DialogsActivity.BROADCAST_ACTION);
        sms.putExtra("NewMessage", true);
        sms.putExtra("Sender_Name", sender_name);
        sms.putExtra("Sender_ID", sender_id);
        sms.putExtra("Text", text);
        sendBroadcast(sms);
    }

    private void sendNotification(String name) {
        if(name != null){
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                    .setContentTitle("Сообщение на JobFinder")
                    .setContentText("Сообщение от " + name)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setVibrate(new long[] { 0, 500, 150, 300 })
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(2259, notificationBuilder.build());
        }
    }

}
