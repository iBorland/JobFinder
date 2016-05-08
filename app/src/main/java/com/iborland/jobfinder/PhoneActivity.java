package com.iborland.jobfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

/**
 * Created by iborland on 28.03.16.
 */
public class PhoneActivity extends AppCompatActivity {

    EditText row;
    ImageView btn;
    Animation top, left, return_left;
    RelativeLayout rel;
    TextView phoneText, anyText;

    String number;
    String code;
    int stape = 1;

    Snackbar mSnackbar;
    View snackbarView;
    TextView snackTextView;
    SendSMS sendSMS;
    UpdateNumber updateNumber;

    User User;
    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        top = AnimationUtils.loadAnimation(this, android.support.design.R.anim.abc_slide_in_top);
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);
        row = (EditText)findViewById(R.id.rowNumber);
        btn = (ImageView)findViewById(R.id.imageView);
        rel = (RelativeLayout)findViewById(R.id.RelInPhone);
        mSnackbar = Snackbar.make(rel, getString(R.string.number_phone), Snackbar.LENGTH_LONG);
        snackbarView = mSnackbar.getView();
        snackTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackTextView.setTextColor(getResources().getColor(R.color.colorText));
        anyText = (TextView)findViewById(R.id.anyText);
        phoneText = (TextView)findViewById(R.id.phoneText);

        if(getIntent().getParcelableExtra("User") != null){
            User = getIntent().getParcelableExtra("User");
        }
        else{
            rel.removeAllViews();
            anyText.setText(getString(R.string.try_again));
            rel.addView(anyText);
            return;
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TEST", "Нажато");
                if(row.getText().toString() == null) return;
                if(stape == 1){
                    if(sendSMS != null && sendSMS.getStatus() == AsyncTask.Status.RUNNING){
                        snackTextView.setText(getString(R.string.message_sending));
                        mSnackbar.show();
                        return;
                    }
                    if(row.length() > 9){
                        if(row.length() == 10 && row.getText().toString().startsWith("9")){
                            number = "7" + row.getText().toString();
                        }
                        if(row.length() == 11 && row.getText().toString().startsWith("7")){
                            number = row.getText().toString();
                        }
                        if(row.length() == 11 && row.getText().toString().startsWith("8")){
                            number = "7" + String.copyValueOf(row.getText().toString().toCharArray(), 1, row.length()-1);
                        }
                        if(row.length() == 12 && row.getText().toString().startsWith("+7")){
                            number = String.copyValueOf(row.getText().toString().toCharArray(), 1, row.length());
                        }
                    }
                    else{
                        snackTextView.setText("Введите номер вашего сотового телефона");
                        mSnackbar.show();
                        return;
                    }
                    if(number == null){
                        snackTextView.setText("Введите настоящий номер вашего сотового телефона");
                        mSnackbar.show();
                        return;
                    }
                    Log.e("NUMBER", "НОМЕР: " + number);
                    code = "" + (1000 + new Random().nextInt(8999));
                    sendSMS = new SendSMS();
                    sendSMS.execute();
                }
                if(stape == 2){
                    if(updateNumber != null && updateNumber.getStatus() == AsyncTask.Status.RUNNING){
                        snackTextView.setText(getString(R.string.code_checking));
                        mSnackbar.show();
                        return;
                    }
                    if(!row.getText().toString().equals(code) && row.getText() != null){
                        anyText.setText(getString(R.string.false_code));
                        anyText.setTextColor(getResources().getColor(R.color.RED));
                        rel.addView(anyText);
                        anyText.startAnimation(left);

                        snackTextView.setText(getString(R.string.false_code));
                        mSnackbar.show();
                        return;
                    }
                    updateNumber = new UpdateNumber();
                    updateNumber.execute();
                }
            }
        });
    }

    class SendSMS extends AsyncTask<Void, Void, Integer>{
        @Override
        protected Integer doInBackground(Void... params) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                String query = "SELECT * FROM `users` WHERE `phone` = '" + number + "'";
                rs = statement.executeQuery(query);
                while(rs.next()){
                    return 1;
                }

                APILoader api = new APILoader("http://sms.ru/sms/send");

                String[] parametres = {"api_id", "to", "text", "from"};
                String[] keys = {"8530CA60-A218-33A7-4FBB-BE25D111391C", number, "Registation code: " + code, "JobFinder"};

                api.addParams(parametres, keys);
                String result = api.execute();

                String res_code = "" + result.charAt(0) + result.charAt(1) + result.charAt(2);
                return Integer.parseInt(res_code);
            }
            catch (Exception e){
                e.printStackTrace();
                return -5;
            }
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            super.onPostExecute(aVoid);
            if(aVoid == 1){
                Toast.makeText(PhoneActivity.this, getString(R.string.phone_used), Toast.LENGTH_SHORT).show();
                phoneText.setText(getResources().getText(R.string.phone_text));
                rel.addView(anyText);
                row.setEnabled(true);
                row.setCursorVisible(true);
                return;
            }
            if(aVoid == 100){
                stape = 2;
                phoneText.setText("СМС с кодом успешно отправлено на номер +" + number + "\n" +
                        "Введите код в окно ниже.");
                row.setEnabled(true);
                row.setCursorVisible(true);
                row.setText("");
                row.setHint(getString(R.string.code_in_sms));
            }
            else{
                phoneText.setText(getString(R.string.error) + "\n\nError code: " + aVoid);
                rel.removeAllViews();
                rel.addView(phoneText);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            row.setEnabled(false);
            row.setCursorVisible(false);
            rel.removeView(anyText);
            phoneText.setText(getString(R.string.sending_sms));
        }
    }

    class UpdateNumber extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                String query = "UPDATE `users` SET `phone` = '" + number + "' WHERE `id` = '" + User.id + "'";
                Log.e("Запрос", query);
                statement.executeUpdate(query);
            }
            catch (Exception e){
                Log.e("Reg", "Ошибка БД ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(PhoneActivity.this, getString(R.string.number_is_saved), Toast.LENGTH_SHORT).show();
            Intent i = new Intent(PhoneActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(i);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            row.setEnabled(false);
            row.setCursorVisible(false);
            try{
                rel.removeView(anyText);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            phoneText.setText(getString(R.string.checked));
        }
    }


}
