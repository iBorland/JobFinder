package com.iborland.jobfinder;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Auth_Activity extends AppCompatActivity {

    RelativeLayout rel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        rel = (RelativeLayout)findViewById(R.id.Rel);

        final Button enter = (Button) findViewById(R.id.button_Enter);
        final Button reg = (Button) findViewById(R.id.button_Reg);
        final EditText login = (EditText)findViewById(R.id.rowLogin);
        final EditText password = (EditText)findViewById(R.id.rowPassword);

        assert enter != null;
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(login.length() < 3){
                    Toast.makeText(Auth_Activity.this, getString(R.string.small_login), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(login.length() > 64){
                    Toast.makeText(Auth_Activity.this, getString(R.string.long_login), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length() < 3){
                    Toast.makeText(Auth_Activity.this, getString(R.string.small_password), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length() > 64){
                    Toast.makeText(Auth_Activity.this, getString(R.string.long_password), Toast.LENGTH_SHORT).show();
                    return;
                }

                Authorizated auth = new Authorizated();
                auth.execute(login.getText().toString(), password.getText().toString());
            }
        });

        assert reg != null;
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Auth_Activity.this, RegActivity.class);
                startActivity(intent);
            }
        });

        if(getIntent().getIntExtra("key", -5) == 5){
            Toast.makeText(Auth_Activity.this, "Аккаунт успешно зарегистрирован", Toast.LENGTH_SHORT).show();
        }

    }

    class Authorizated extends AsyncTask<String, Void, Integer>{

        int id;
        String Token;
        ProgressDialog dialog;

        @Override
        protected Integer doInBackground(String... params) {
            try {
                String query = "SELECT * FROM `users` WHERE `Login` = '" + params[0] +
                        "' AND `Password` = '" + params[1] + "'";
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection;
                Statement statement;
                ResultSet rs;
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                boolean finded = false;
                while (rs.next()) {
                    id = rs.getInt("id");
                    Token = rs.getString("Token");
                    finded = true;
                }
                connection.close();
                statement.close();
                rs.close();
                if(finded) return 1;
            }
            catch (Exception e){
                Log.e("Error", "Ошибка загрузки БД ", e);
                return 0;
            }
            return -5;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Auth_Activity.this);
            dialog.setMessage(getString(R.string.loaded));
            dialog.show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            dialog.dismiss();
            if(integer == 0){
                Toast.makeText(Auth_Activity.this, getString(R.string.error_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            if(integer == -5){
                Toast.makeText(Auth_Activity.this, getString(R.string.error_auth), Toast.LENGTH_SHORT).show();
                return;
            }
            DBHelper mDatabaseHelper;
            SQLiteDatabase mSqLiteDatabase;

            mDatabaseHelper = new DBHelper(getApplicationContext(), "userinfo.db", null, 2);
            mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("Token", Token);
            mSqLiteDatabase.insert("user", null, values);
            Intent intent = new Intent(Auth_Activity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mSqLiteDatabase.close();
            mDatabaseHelper.close();
            finish();
            startActivity(intent);
        }
    }

}
