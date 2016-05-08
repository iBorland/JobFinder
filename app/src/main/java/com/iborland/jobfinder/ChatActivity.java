package com.iborland.jobfinder;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by iBorland on 12.04.2016.
 */
public class ChatActivity extends AppCompatActivity {

    User user, partner;
    ProgressDialog progressDialog;
    boolean sending = false;
    LinkedList<Message> messages = new LinkedList<>();
    ArrayList<Integer> ids = new ArrayList<>();

    LinearLayout rel;
    EditText row;

    LoadMsgs loadMsgs;
    SendMessage sendMessage;
    BroadcastReceiver br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rel = (LinearLayout) findViewById(R.id.linear_chat);
        row = (EditText)findViewById(R.id.row_Chat);

        ImageButton send = (ImageButton)findViewById(R.id.button_chat);

        if (send != null) {
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(row.length() < 1) return;
                    if(row.length() > 512){
                        Toast.makeText(ChatActivity.this, "Слишком длинный текст", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendMessage = new SendMessage();
                    sendMessage.execute();
                }
            });
        }

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_chat);

        user = getIntent().getParcelableExtra("User");
        final int partner_id = getIntent().getIntExtra("Partner", -5);
        if(partner_id == -5 || user == null) {
            ErrorMessage(getString(R.string.error_connection));
            return;
        }
        Log.e("Partner ID", "" + partner_id);
        partner = new User(partner_id, "123", true, true, ChatActivity.this);
        Log.e("Partner name", "" + partner.login);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(partner.login);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int user_id = intent.getIntExtra("Sender_ID", 0);

                if(user_id == partner_id) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(2259);
                    loadMsgs = new LoadMsgs();
                    loadMsgs.execute();
                }
            }
        };

        if(partner_id == user.id){
            int padding_in_px = (int) (10 * getResources().getDisplayMetrics().density + 0.5f);
            TextView start_msg = new TextView(ChatActivity.this);
            start_msg.setText(getString(R.string.not_yourself));
            start_msg.setTextColor(getResources().getColor(R.color.colorBlackText));
            start_msg.setTextSize(16);
            start_msg.setGravity(Gravity.CENTER);
            start_msg.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
            progressBar.setVisibility(View.GONE);
            rel.addView(start_msg);
            row.setEnabled(false);
            return;
        }

        loadMsgs = new LoadMsgs();
        loadMsgs.execute();
    }

    public void ErrorMessage(String message){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Ошибка");
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
        return;
    }

    public boolean addMessage(Message message){
        for(int i = 0; i != ids.size(); i++){
            if(message.id == ids.get(i)) return false;
        }
        int padding_in_px = (int) (10 * getResources().getDisplayMetrics().density + 0.5f);
        TextView msg = new TextView(ChatActivity.this);
        msg.setText(message.text);
        msg.setTextColor(getResources().getColor(R.color.white));
        msg.setTextSize(14);
        msg.setPadding(padding_in_px + (padding_in_px / 2), padding_in_px + (padding_in_px / 2), padding_in_px + (padding_in_px / 2), padding_in_px + (padding_in_px / 2));
        rel.addView(msg);
        if(message.sender_id == user.id){ // ridht
            msg.setBackground(getResources().getDrawable(R.drawable.right_message));
            msg.setGravity(Gravity.LEFT);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)msg.getLayoutParams();
            lp.setMargins(padding_in_px * 10, padding_in_px / 2, padding_in_px, padding_in_px);
            msg.setLayoutParams(lp);
        }
        else{
            msg.setBackground(getResources().getDrawable(R.drawable.left_message));
            msg.setGravity(Gravity.LEFT);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)msg.getLayoutParams();
            lp.setMargins(padding_in_px, padding_in_px / 2, padding_in_px * 10, padding_in_px);
            msg.setLayoutParams(lp);
        }
        ids.add(message.id);
        return true;
    }

    class LoadMsgs extends AsyncTask<Void, Void, Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(sending == true && progressDialog.isShowing() == true) progressDialog.hide();
            ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_chat);
            progressBar.setVisibility(View.INVISIBLE);
            if(integer == -4){
                TextView start_msg = new TextView(ChatActivity.this);
                start_msg.setText(getString(R.string.messages_not_found));
                start_msg.setTextColor(getResources().getColor(R.color.colorBlackText));
                start_msg.setTextSize(16);
                start_msg.setGravity(Gravity.CENTER);
                int padding_in_px = (int) (10 * getResources().getDisplayMetrics().density + 0.5f);
                start_msg.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
                rel.addView(start_msg);
            }
            if(integer == -5){
                ErrorMessage(getString(R.string.error_connection));
                return;
            }
            if(integer == 1){
                for(int i = messages.size() - 1; i >= 0; i--){
                    addMessage(messages.get(i));
                }
                Timer timer = new Timer();
                TimerTask task = new ScrollUpdated();
                timer.schedule(task, 10);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String query = "SELECT * FROM `messages` WHERE `sender_id` = '" + user.id + "' AND `recipient_id` = " +
                        "'" + partner.id + "'";
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = null;
                Statement statement = null;
                ResultSet rs = null;
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()){
                    Message buffer = new Message(true);
                    buffer.id = rs.getInt("id");
                    buffer.sender_id = rs.getInt("sender_id");
                    buffer.recipient_id = rs.getInt("recipient_id");
                    buffer.date = Long.parseLong(rs.getString("date"));
                    buffer.sender_login = rs.getString("sender_login");
                    buffer.recipient_login = rs.getString("recipient_login");
                    buffer.text = rs.getString("text");
                    messages.add(buffer);
                }
                rs = null;
                query = "SELECT * FROM `messages` WHERE `sender_id` = '" + partner.id + "' AND `recipient_id` = " +
                        "'" + user.id + "'";
                rs = statement.executeQuery(query);
                while (rs.next()){
                    Message buffer = new Message(true);
                    buffer.id = rs.getInt("id");
                    buffer.sender_id = rs.getInt("sender_id");
                    buffer.recipient_id = rs.getInt("recipient_id");
                    buffer.date = Long.parseLong(rs.getString("date"));
                    buffer.sender_login = rs.getString("sender_login");
                    buffer.recipient_login = rs.getString("recipient_login");
                    buffer.text = rs.getString("text");
                    messages.add(buffer);
                }
                if(messages.size() == 0) return -4;

                Collections.sort(messages);

                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return -5;
            }
        }
    }

    class SendMessage extends AsyncTask<Void, Void, Integer>{

        String text;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            text = row.getText().toString();
            row.setText("");
            progressDialog = new ProgressDialog(ChatActivity.this);
            progressDialog.setMessage(getString(R.string.sending));
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            sending = true;
            loadMsgs = new LoadMsgs();
            loadMsgs.execute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Connection connection = null;
                Statement statement = null;
                ResultSet rs = null;
                long date = System.currentTimeMillis() / 1000;
                String query = "INSERT INTO `messages` (`sender_id`, `sender_login`,`recipient_id`,`recipient_login`,`text`,`date`" +
                        ") VALUES ('" + user.id + "', '" + user.login + "','" + partner.id + "'," +
                        "'" + partner.login + "','" + text + "','" + date + "')";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                statement.executeUpdate(query);

                APILoader gcm = new APILoader("http://api.jobfinder.ru.com/gcm.php");
                gcm.addParams(new String[] {"regID", "type", "sender_id", "sender_name"},
                        new String[] {partner.msg_id, "1", "" + user.id, user.login});
                gcm.execute();
                gcm = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class ScrollUpdated extends TimerTask{

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.scroll_chat).scrollTo(0, rel.getHeight());
                }
            });
        }
    }

    class UpdateMessage extends TimerTask{

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(loadMsgs.getStatus() == AsyncTask.Status.FINISHED){
                        loadMsgs = new LoadMsgs();
                        loadMsgs.execute();
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(br);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intFilt = new IntentFilter(DialogsActivity.BROADCAST_ACTION);
        registerReceiver(br, intFilt);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)finish();
        return super.onOptionsItemSelected(item);
    }
}
