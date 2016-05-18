package com.iborland.jobfinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by iBorland on 02.05.2016.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        int type = Integer.parseInt(data.getString("type"));
        Log.e("ПРИНЯЛ СООБЩЕНИЕ", "TYPE = " + type);
        if(type == 1){
            String sender_name = data.getString("sender_name");
            int sender_id = Integer.parseInt(data.getString("sender_id"));

            sendNotification(type, sender_name);
            Intent sms = new Intent(DialogsActivity.BROADCAST_ACTION);
            sms.putExtra("NewMessage", true);
            sms.putExtra("Sender_Name", sender_name);
            sms.putExtra("Sender_ID", sender_id);
            sendBroadcast(sms);
        }
        if(type == 2){
            String sender_name = data.getString("sender_name");
            sendNotification(type, sender_name);
        }
        if(type == 3){
            String sender_name = data.getString("executor_name");
            sendNotification(type, sender_name);
        }
        if(type == 4){
            String sender_name = data.getString("sender_name");
            sendNotification(type, sender_name);
        }
    }

    private void sendNotification(int type, String... params) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_email);
        switch(type){
            case 1: {
                notificationBuilder.setContentTitle("Сообщение на JobFinder");
                notificationBuilder.setContentText("Сообщение от " + params[0]);
                break;
            }
            case 2: {
                notificationBuilder.setContentTitle("JobFinder");
                notificationBuilder.setContentText(params[0] + " принял ваше задание");
                break;
            }
            case 3: {
                notificationBuilder.setContentTitle("JobFinder");
                notificationBuilder.setContentText(params[0] + " выполнил ваше задание");
                break;
            }
            case 4: {
                notificationBuilder.setContentTitle("JobFinder");
                notificationBuilder.setContentText(params[0] + "подтвердил выполнение заказа");
                break;
            }

        }
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSound(defaultSoundUri);
        notificationBuilder.setVibrate(new long[] { 0, 500, 150, 300 });
        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationManager.notify(2260 - type, notificationBuilder.build());
        }
        else{
            NotificationManagerCompat notify = NotificationManagerCompat.from(this);
            notify.notify(2260 - type, notificationBuilder.getNotification());
        }
    }



}
