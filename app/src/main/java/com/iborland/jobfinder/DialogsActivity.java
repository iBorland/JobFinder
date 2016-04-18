package com.iborland.jobfinder;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by iBorland on 12.04.2016.
 */
public class DialogsActivity extends AppCompatActivity{

    ProgressBar progressBar;
    ListView list;
    RelativeLayout rel;

    User user;
    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    final int MAX_LENGTH = 256;

    Message[] messages = new Message[MAX_LENGTH];
    Dialog[] dialogs = new Dialog[MAX_LENGTH];
    int amount_messages = 0;
    int amount_dialogs = 0;
    ActionBar bar;
    public final static String BROADCAST_ACTION = "com.iborland.jobfinder.messages";
    BroadcastReceiver broadcastReceiver;
    boolean first_loaded = true;
    LoadedMessage loadedMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        user = getIntent().getParcelableExtra("User");
        progressBar = (ProgressBar)findViewById(R.id.loading_messeges);
        list = (ListView)findViewById(R.id.listview_message);
        rel = (RelativeLayout)findViewById(R.id.messages_relative);
        bar = getSupportActionBar();

        Intent intent = new Intent(DialogsActivity.this, MessageService.class);
        intent.putExtra("User", user);
        startService(intent);

        rel.removeView(list);

        if(user == null){
            TextView message = new TextView(DialogsActivity.this);
            message.setText(getString(R.string.try_again));
            message.setTextColor(getResources().getColor(R.color.colorBlackText));
            message.setTextSize(16);
            message.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

            rel.removeView(progressBar);
            rel.addView(message);
            return;
        }

        loadedMessage = new LoadedMessage();
        loadedMessage.execute();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean newMessage = intent.getBooleanExtra("NewMessage", false);
                if(newMessage == true){
                    if(loadedMessage != null && loadedMessage.getStatus() == AsyncTask.Status.RUNNING) return;
                    loadedMessage = new LoadedMessage();
                    loadedMessage.execute();
                    Log.e("ТЕСТ", "Ресивер сработал");
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(77);
                }
            }
        };

    }

    class LoadedMessage extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar.setTitle(getString(R.string.update));
        }

        @Override
        protected void onPostExecute(final Integer integer) {
            super.onPostExecute(integer);
            bar.setTitle(getString(R.string.messages));
            if(integer == -4){
                TextView message = new TextView(DialogsActivity.this);
                message.setText(getString(R.string.messages_not_found));
                message.setTextColor(getResources().getColor(R.color.colorBlackText));
                message.setTextSize(16);
                message.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

                rel.removeView(progressBar);
                rel.addView(message);
                return;
            }
            if(integer == -5){
                TextView message = new TextView(DialogsActivity.this);
                message.setText(getString(R.string.error_connection));
                message.setTextColor(getResources().getColor(R.color.colorBlackText));
                message.setTextSize(16);
                message.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

                rel.removeView(progressBar);
                rel.addView(message);
                return;
            }

            ArrayList<HashMap<String, Object>> buffer = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> map;

            user.lost_msg = dialogs[0].id;
            user.updateUser(false);

            String[] from = {"Author", "Text"};
            int[] to = {R.id.name_author, R.id.text_message};

            for(int i = 0; i != amount_dialogs; i++){
                map = new HashMap<String, Object>();
                map.put(from[0], dialogs[i].author);
                String msg = "";
                if(dialogs[i].lost_author.equals(user.login)){
                    msg = "Вы: " + dialogs[i].lost_text;
                }
                else msg = dialogs[i].lost_text;
                map.put(from[1], msg);
                buffer.add(map);
            }

            if(first_loaded == true) rel.addView(list);
            SimpleAdapter adapter = new SimpleAdapter(DialogsActivity.this, buffer, R.layout.list_messages, from, to);
            list.setAdapter(adapter);
            rel.removeView(progressBar);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(DialogsActivity.this, ChatActivity.class);
                    intent.putExtra("User", user);
                    intent.putExtra("Partner", dialogs[position].author_id);
                    startActivity(intent);
                }
            });
            first_loaded = false;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String query = "SELECT * FROM `messages` WHERE `sender_id` = '" + user.id + "' OR `recipient_id` = " +
                        "'" + user.id + "'";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                if(first_loaded == false){
                    amount_messages = 0;
                    amount_dialogs = 0;
                }
                while(rs.next()){
                    if(amount_messages >= 255) break;
                    Message buffer = new Message(true);
                    buffer.id = rs.getInt("id");
                    buffer.sender_id = rs.getInt("sender_id");
                    buffer.recipient_id = rs.getInt("recipient_id");
                    buffer.date = Long.parseLong(rs.getString("date"));
                    buffer.sender_login = rs.getString("sender_login");
                    buffer.recipient_login = rs.getString("recipient_login");
                    buffer.text = rs.getString("text");
                    messages[amount_messages] = buffer;
                    amount_messages++;
                }

                if(amount_messages == 0) return -4;

                Arrays.sort(messages, 0, amount_messages);

                for(int i = 0; i != amount_messages; i++) {

                    String auth = messages[i].recipient_login;
                    int auth_id = messages[i].recipient_id;
                    if (auth.equals(user.login)) {
                        auth = messages[i].sender_login;
                        auth_id = messages[i].sender_id;
                    }

                    if(i == 0) {
                        Dialog d = new Dialog(auth, auth_id, messages[i].text, messages[i].sender_login, messages[i].id);
                        dialogs[amount_dialogs] = d;
                        amount_dialogs++;
                        continue;
                    }

                    boolean finded = false;

                    for(int q = 0; q != amount_dialogs; q++){
                        if(dialogs[q] != null && dialogs[q].author.equals(auth)){
                            finded = true;
                            break;
                        }
                    }

                    if(finded == true) continue;
                    else{
                        Dialog d = new Dialog(auth, auth_id, messages[i].text, messages[i].sender_login, messages[i].id);
                        dialogs[amount_dialogs] = d;
                        amount_dialogs++;
                    }

                }
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return -5;
            }
        }
    }

    class Dialog{
        String author, lost_text, lost_author;
        int author_id, id;

        Dialog(String auth, int id, String l_text, String l_author, int l_id){
            author = auth;
            author_id = id;
            lost_text = l_text;
            lost_author = l_author;
            id = l_id;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intFilt = new IntentFilter(DialogsActivity.BROADCAST_ACTION);
        registerReceiver(broadcastReceiver, intFilt);
    }
}
