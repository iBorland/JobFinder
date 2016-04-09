package com.iborland.jobfinder;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by iborland on 19.03.16.
 */
public class AuthActivity extends AppCompatActivity {

    TextView text;
    EditText rowLogin;
    EditText rowPassword;
    Button enter;
    Button reg;
    ProgressBar progressBar;
    RelativeLayout rl;


    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    Snackbar mSnackbar;
    View snackbarView;
    TextView snackTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        if(!MainActivity.isOnline(AuthActivity.this)){
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AuthActivity.this);
            builder.setTitle("Ошибка");
            builder.setMessage("У вас отсутствует подключение к интернету.\n\n" +
                    "Проверьте ваше подключение и повторите попытку снова.");
            builder.setCancelable(false);
            builder.setPositiveButton("Выйти", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });
            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();
        }
        text = (TextView)findViewById(R.id.text);
        rowLogin = (EditText)findViewById(R.id.rowLogin);
        rowPassword = (EditText)findViewById(R.id.rowPassword);
        enter = (Button)findViewById(R.id.button_Enter);
        reg = (Button)findViewById(R.id.button_Reg);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        rl = (RelativeLayout)findViewById(R.id.Rel);
        mSnackbar = Snackbar.make(rl, "Слишком короткий логин", Snackbar.LENGTH_LONG);
        snackbarView = mSnackbar.getView();
        snackTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);

        snackTextView.setTextColor(getResources().getColor(R.color.colorText));

        int key_reg = getIntent().getIntExtra("key", -5);

        if(key_reg == 1) {
            snackTextView.setText("Логин уже занят");
            mSnackbar.show();
        }
        if(key_reg == 2) {
            snackTextView.setText("Email уже занят");
            mSnackbar.show();
        }
        if(key_reg == 5) {
            snackTextView.setText("Аккаунт успешно зарегистрирован");
            mSnackbar.show();
        }

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rowLogin.length() < 3) {
                    snackTextView.setText("Слишком короткий логин");
                    mSnackbar.show();
                    return;
                }
                if (rowLogin.length() > 255) {
                    snackTextView.setText("Слишком длинный логин");
                    mSnackbar.show();
                    return;
                }
                if (rowPassword.length() < 6) {
                    snackTextView.setText("Слишком короткий пароль");
                    mSnackbar.show();
                    return;
                }
                if (rowPassword.length() > 255) {
                    snackTextView.setText("Слишком длинный пароль");
                    mSnackbar.show();
                    return;
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(enter.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                if (MainActivity.isOnline(getApplicationContext()) == false) {
                    Toast.makeText(AuthActivity.this, "Ошибка подключения к интернету", Toast.LENGTH_SHORT).show();
                    return;
                }
                LoadUser loadUser = new LoadUser();
                loadUser.execute();
            }
        });

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthActivity.this, RegActivity.class);
                startActivity(intent);
            }
        });

    }

    class LoadUser extends AsyncTask<Void, Void, Void>
    {
        String query;
        int id;
        String Token;
        boolean finded = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            query = "SELECT * FROM `users` WHERE `Login` = '" + rowLogin.getText().toString() +
                    "' AND `Password` = '" + rowPassword.getText().toString() + "'";
            rl.removeAllViews();
            rl.addView(progressBar);
            text.setText("Загрузка данных\nОдну минуточку...");
            rl.addView(text);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(finded == true){
                DBHelper mDatabaseHelper;
                SQLiteDatabase mSqLiteDatabase;

                mDatabaseHelper = new DBHelper(getApplicationContext(), "userinfo.db", null, 2);
                mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("Token", Token);
                mSqLiteDatabase.insert("user", null, values);
                Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
            }
            else{
                Log.e("Log", "Пользователь не найден в БД");
                rl.removeAllViews();
                rl.addView(rowLogin);
                rl.addView(rowPassword);
                rl.addView(enter);
                rl.addView(reg);
                text.setText("Авторизация");
                rl.addView(text);
                snackTextView.setText("Пользователь с таким логином и паролем не найден");
                mSnackbar.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()) {
                    id = rs.getInt("id");
                    Token = rs.getString("Token");
                    finded = true;
                    Log.e("Log", "Пользователь найден. Его ID: " + id);
                    break;
                }
            }
            catch (Exception e){
                Log.e("Error", "Ошибка загрузки БД ", e);
            }
            return null;
        }
    }
}
