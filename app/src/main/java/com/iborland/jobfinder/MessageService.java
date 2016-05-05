package com.iborland.jobfinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by iBorland on 16.04.2016.
 */
public class MessageService extends Service {

    User user;
    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    CheckMsg checkMsg;
    Timer timer;
    TimerTask timerTask;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stopSelf();
        user = intent.getParcelableExtra("User");
        if(timer == null) {
            timerTask = new Timertask();
            timer = new Timer();
            timer.schedule(timerTask, 15000, 15000);
            Log.e("SERVICE", "onStartCommand");
        }
        return START_FLAG_REDELIVERY;
    }

    @Override
    public void onCreate() {
        stopSelf();
        Log.e("SERVICE", "onCreate");
        super.onCreate();
    }

    class Timertask extends TimerTask{

        @Override
        public void run(){
            checkMsg = new CheckMsg();
        }

    }

    class CheckMsg extends Thread{

        private CheckMsg(){run();};

        public void run() {
            int buffer_id = 0;
            if(user == null) return;
            String query = "SELECT * FROM `messages` WHERE `recipient_id` = '" + user.id + "' ORDER BY `id` DESC";
            Log.e("Service ", "Запрос: " + query);
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()){
                    buffer_id = rs.getInt("id");
                    if(buffer_id != 0 && user.lost_msg != buffer_id){
                        user.lost_msg = buffer_id;
                        user.update(new String[] {User.DB_LOST_MESSAGE}, new Object[] {user.lost_msg});
                        Message buffer = new Message(true);
                        buffer.id = rs.getInt("id");
                        buffer.sender_id = rs.getInt("sender_id");
                        buffer.recipient_id = rs.getInt("recipient_id");
                        buffer.date = Long.parseLong(rs.getString("date"));
                        buffer.sender_login = rs.getString("sender_login");
                        buffer.recipient_login = rs.getString("recipient_login");
                        buffer.text = rs.getString("text");
                        sendNotify(buffer);
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void sendNotify(Message msg){
        Log.e("Отправ мcгу без напрягу", "satg");

        Intent notyfy_intent = new Intent(this, ChatActivity.class);
        notyfy_intent.putExtra("User", user);
        notyfy_intent.putExtra("Partner", msg.sender_id);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notyfy_intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(android.R.drawable.ic_dialog_email);
        builder.setTicker(getString(R.string.new_ticker));
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        builder.setContentTitle(getString(R.string.message_text) + msg.sender_login);
        builder.setContentText(msg.text);
        builder.setPriority(Notification.PRIORITY_DEFAULT);
        builder.setDefaults(Notification.DEFAULT_ALL);

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(77, notification);

        boolean new_messages = true;
        Intent broadcast_intent = new Intent(DialogsActivity.BROADCAST_ACTION);
        broadcast_intent.putExtra("NewMessage", new_messages);
        broadcast_intent.putExtra("User", user);
        broadcast_intent.putExtra("Message", msg);
        sendBroadcast(broadcast_intent);
        return;
    }
}
