package com.iborland.jobfinder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by iBorland on 12.04.2016.
 */
public class MessagesActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ListView list;
    RelativeLayout rel;

    User user;
    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    Message[] messages = new Message[256];
    Dialog[] dialogs = new Dialog[256];
    int amount_messages = 0;
    int amount_dialogs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        user = getIntent().getParcelableExtra("User");
        progressBar = (ProgressBar)findViewById(R.id.loading_messeges);
        list = (ListView)findViewById(R.id.listview_message);
        rel = (RelativeLayout)findViewById(R.id.messages_relative);

        rel.removeView(list);

        if(user == null){
            TextView message = new TextView(MessagesActivity.this);
            message.setText(getString(R.string.try_again));
            message.setTextColor(getResources().getColor(R.color.colorBlackText));
            message.setTextSize(16);
            message.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

            rel.removeView(progressBar);
            rel.addView(message);
            return;
        }


    }

    class LoadedMessage extends AsyncTask<Void, Void, Integer>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == -4){
                TextView message = new TextView(MessagesActivity.this);
                message.setText(getString(R.string.messages_not_found));
                message.setTextColor(getResources().getColor(R.color.colorBlackText));
                message.setTextSize(16);
                message.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

                rel.removeView(progressBar);
                rel.addView(message);
                return;
            }
            if(integer == -5){
                TextView message = new TextView(MessagesActivity.this);
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

            String[] from = {"Author", "Text"};
            int[] to = {R.id.name_author, R.id.text_message};

            for(int i = 0; i != amount_dialogs; i++){
                map = new HashMap<String, Object>();
                map.put(from[0], dialogs[i].lost_author);
                String msg = "";
                if(dialogs[i].lost_author.equals(user.login)){
                    msg = "Вы: " + dialogs[i].lost_text;
                }
                else msg = dialogs[i].lost_text;
                map.put(from[1], msg);
            }

            SimpleAdapter adapter = new SimpleAdapter(MessagesActivity.this, buffer, R.layout.list_messages, from, to);
            list.setAdapter(adapter);
            rel.removeView(progressBar);
            rel.addView(list);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String query = "SELECT * FROM `message` WHERE `sender_id` = '" + user.id + "' OR `recipient_id` = " +
                        "'" + user.id + "'";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while(rs.next()){
                    Message buffer = new Message();
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

                for(int i = 0; i != amount_messages; i++){

                    boolean finded = false;

                    for(int a = 0; a != amount_dialogs; a++){

                        if(dialogs[a] != null && dialogs[a].author.equals(messages[i].sender_login)){
                            finded = true;
                            break;
                        }
                        if(finded == true) continue;

                        String author = messages[i].sender_login;
                        if(author.equals(user.login)) author = messages[i].recipient_login;
                        dialogs[amount_dialogs] = new Dialog(author, messages[i].text, messages[i].sender_login);
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

    class Message implements Comparable{

        int id, sender_id, recipient_id;
        long date;
        String sender_login, recipient_login, text;

        public int compareTo(Object obj)
        {
            Message tmp = (Message)obj;
            if(this.date < tmp.date) return 1;
            else if(this.date > tmp.date) return -1;
            return 0;
        }
    }

    class Dialog{
        String author, lost_text, lost_author;

        Dialog(String auth, String l_text, String l_author){
            author = auth;
            lost_text = l_text;
            lost_author = l_author;
        }
    }

}
