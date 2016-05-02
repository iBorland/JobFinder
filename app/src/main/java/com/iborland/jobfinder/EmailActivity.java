package com.iborland.jobfinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by iBorland on 29.04.2016.
 */
public class EmailActivity extends AppCompatActivity {

    User user;
    TextView header, enter, text;
    Button next;
    EditText row;

    public static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    int step = 1;
    int code;
    String buffer_mail;

    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        user = getIntent().getParcelableExtra("User");
        header = (TextView)findViewById(R.id.mail_header);
        enter = (TextView)findViewById(R.id.mail_enter);
        text = (TextView)findViewById(R.id.mail_text);
        next = (Button)findViewById(R.id.mail_next);
        row = (EditText)findViewById(R.id.mail_row);

        row.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER){
                    if(step == 1){
                        if(!mail_validate(row.getText().toString())){
                            sendMessage(EmailActivity.this, getString(R.string.error), "Не верно введён Email адрес");
                            return false;
                        }
                        buffer_mail = row.getText().toString();
                        SendCode s = new SendCode();
                        s.execute();
                    }
                    if(step == 2){
                        if(row.length() > 0 && Integer.parseInt(row.getText().toString()) == code){
                            user.email = buffer_mail;
                            UpdateMail u = new UpdateMail();
                            u.execute();
                            sendMessage(EmailActivity.this, getString(R.string.success), "Адрес электронной почты успешно привязан");
                            step = 3;
                        }
                        else{
                            sendMessage(EmailActivity.this, getString(R.string.error), "Неверный код");
                        }
                    }
                }
                return false;
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(step == 1){
                    if(!mail_validate(row.getText().toString())){
                        sendMessage(EmailActivity.this, getString(R.string.error), "Не верно введён Email адрес");
                        return;
                    }
                    buffer_mail = row.getText().toString();
                    SendCode s = new SendCode();
                    s.execute();
                }
                if(step == 2){
                    if(row.length() > 0 && Integer.parseInt(row.getText().toString()) == code){
                        user.email = buffer_mail;
                        UpdateMail u = new UpdateMail();
                        u.execute();
                        sendMessage(EmailActivity.this, getString(R.string.success), "Адрес электронной почты успешно привязан");
                        step = 3;
                    }
                    else{
                        sendMessage(EmailActivity.this, getString(R.string.error), "Неверный код");
                    }
                }
            }
        });

    }

    public static boolean mail_validate(String str) {
        Matcher matcher = EMAIL_PATTERN .matcher(str);
        return matcher.find();
    }

    public void sendMessage(Context context, String header, String message){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle(header);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if(step == 3){
                    Intent intent = new Intent(EmailActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    class SendCode extends AsyncTask<Void, Void, Integer>{

        int amount = 0;

        @Override
        protected Integer doInBackground(Void... params) {

            try{
                String query = "SELECT * FROM `users` WHERE `Email` = '" + buffer_mail + "'";
                Log.e("QUERY", query);
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()) amount++;
                connection.close(); connection = null;
                rs.close(); rs = null;
            }
            catch (Exception e){
                e.printStackTrace();
                return -1;
            }
            Log.e("ASGAGAGASG", "AMOUNT " + amount);

            if(amount > 0){
                return -5;
            }

            try {
                code = new Random().nextInt(89999) + 10000;

                String url = "http://api.jobfinder.ru.com/mailer.php";
                APILoader api = new APILoader(url);
                String[] parametres = {"t", "s", "m", "f"};
                String[] keys = {buffer_mail, "Accept Email", "Code: " + code, "admin@jobfinder.ru.com"};

                api.addParams(parametres, keys);
                String result = api.execute();
                Log.e("RESULT", result);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
            return 1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            row.setEnabled(false);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            row.setEnabled(true);
            if(integer == -5){
                sendMessage(EmailActivity.this, getString(R.string.error), "Введёный Вами адрес электронной почты уже используется");
                return;
            }
            if(integer == 1) {
                row.setText("");
                row.setInputType(0x00000002);
                row.setHint(getString(R.string.mail_hint_code));
                enter.setText(getString(R.string.mail_ented_code));
                step = 2;
            }
        }
    }

    class UpdateMail extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String query = "UPDATE `users` SET `Email` = '" + buffer_mail + "' WHERE `id` = '" + user.id + "'";
                Log.e("QUERY", query);
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                statement.executeUpdate(query);
                connection.close();
                connection = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
