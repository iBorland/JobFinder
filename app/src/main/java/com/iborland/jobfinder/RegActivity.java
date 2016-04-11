package com.iborland.jobfinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by iborland on 20.03.16.
 */
public class RegActivity extends AppCompatActivity {


    Animation top;
    Animation left;
    Animation return_left;

    EditText rowName, rowSurname, rowLogin, rowPassword, rowAge;
    TextView rowCity;
    Button btnNext;
    EditText rowCityNew;

    String[] citys;

    RelativeLayout rel;
    LinearLayout lin;
    ScrollView scrollView;
    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    boolean default_city = true;
    boolean city_select = false;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        citys = getResources().getStringArray(R.array.citys);

        rel = (RelativeLayout)findViewById(R.id.Rel);
        scrollView = (ScrollView)findViewById(R.id.scrollView4);
        lin = (LinearLayout)findViewById(R.id.Linnnn);

        rowName = (EditText)findViewById(R.id.rowName);
        rowSurname = (EditText)findViewById(R.id.rowSurname);
        rowLogin = (EditText)findViewById(R.id.rowLogin);
        rowPassword = (EditText)findViewById(R.id.rowPassword);
        rowAge = (EditText)findViewById(R.id.rowAge);
        rowCity = (TextView)findViewById(R.id.rowCity);
        btnNext = (Button)findViewById(R.id.BtnNext);

        top = AnimationUtils.loadAnimation(this, android.support.design.R.anim.abc_slide_in_top);
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);

        lin.setBaselineAlignedChildIndex(5);

        rowCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (default_city == true) {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(RegActivity.this);
                    builder.setTitle(getString(R.string.selected_city));
                    builder.setCancelable(false);
                    builder.setItems(citys, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == (citys.length - 1)) {
                                default_city = false;
                                dialog.cancel();
                                lin.removeView(rowCity);
                                rowCityNew = new EditText(getApplicationContext());
                                rowCityNew.setHint(getString(R.string.enter_city));

                                int padding_in_dp_16 = 16;
                                int padding_in_dp_10 = 10;
                                final float scale = getResources().getDisplayMetrics().density;
                                int padding_in_px_16 = (int) (padding_in_dp_16 * scale + 0.5f);
                                int padding_in_px_10 = (int) (padding_in_dp_10 * scale + 0.5f);
                                rowCityNew.setTextColor(getResources().getColor(R.color.colorBlackText));
                                rowCityNew.setPadding(padding_in_px_16, padding_in_px_10, padding_in_px_16, padding_in_px_16);
                                rowCityNew.setMaxLines(1);
                                rowCityNew.setSingleLine();
                                lin.addView(rowCityNew);
                            } else {
                                rowCity.setText(citys[which]);
                                city_select = true;
                                dialog.cancel();
                            }
                        }
                    });
                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rowName.getText().length() < 3){
                    ErrorMessage("Вы ввели слишком короткое имя\n\n" +
                            "Имя пользователя не может быть короче 3-ёх символов и длиннее 32-ух");
                    return;
                }
                if(rowName.getText().length() > 31){
                    ErrorMessage("Вы ввели слишком длинное имя\n\n" +
                            "Имя пользователя не может быть короче 3-ёх символов и длиннее 32-ух");
                    return;
                }
                if(rowSurname.getText().length() < 3){
                    ErrorMessage("Вы ввели слишком короткую фамилию\n\n" +
                            "Фамилия пользователя не может быть короче 3-ёх символов и длиннее 32-ух");
                    return;
                }
                if(rowSurname.getText().length() > 31){
                    ErrorMessage("Вы ввели слишком длинную фамилию\n\n" +
                            "Фамилия пользователя не может быть короче 3-ёх символов и длиннее 32-ух");
                    return;
                }
                if(!LoginValidator(rowLogin.getText().toString())){
                    ErrorMessage("Вы введи некорректный логин.\n\n" +
                            "Логин может состоять только из символов латинского алфавита, " +
                            "содержать цифры и символ подчёркивания.\n\n" +
                            "Логин не может быть короче 3-ёх символов и длиннее 32-и символов");
                    return;
                }
                if(!PasswordValidator(rowPassword.getText().toString())){
                    ErrorMessage("Вы введи некорректный пароль.\n\n" +
                            "Пароль может состоять только из символов латинского алфавита, " +
                            "содержать цифры и символ подчёркивания.\n\n" +
                            "Пароль не может быть короче 6-и символов и длиннее 16 символов");
                    return;
                }
                int age = Integer.parseInt(rowAge.getText().toString());
                if(age < 16){
                    ErrorMessage("Ваш возраст не позволяет вам пользоваться услугами нашего приложения." +
                            "\n\nНам очень жаль :(");
                    return;
                }
                if(age >= 70){
                    ErrorMessage("Да ну, не верю что Вам столько лет ;)");
                    return;
                }
                if(default_city == true)
                {
                    if(city_select != true){
                        ErrorMessage("Нам нужно знать Ваш город, без него мы не сможем подобрать вам интересные услуги.\n\n" +
                                "Пожалуйста, укажите ваш город");
                        return;
                    }
                }
                else{
                    if(rowCityNew.getText().length() < 3){
                        ErrorMessage("Слишком короткое название города.\nТаких не бывает.");
                        return;
                    }
                    if(rowCityNew.getText().length() > 63){
                        ErrorMessage("Слишком длинное название города.");
                        return;
                    }
                    city_select = true;
                }
                CreateUser createUser = new CreateUser();
                createUser.execute();
            }
        });

    }

    class CreateUser extends AsyncTask<Void, Void, Integer>
    {
        String name, surname, login, password, city;
        int age;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(city_select == false){
                ErrorMessage("Нам нужно знать Ваш город, без него мы не сможем подобрать вам интересные услуги.\n\n" +
                        "Пожалуйста, укажите ваш город");
                return;
            }
            login = rowLogin.getText().toString();
            password = rowPassword.getText().toString();
            name = rowName.getText().toString();
            surname = rowSurname.getText().toString();
            age = Integer.parseInt(rowAge.getText().toString());
            if(default_city == true)
                city = rowCity.getText().toString();
            else
                city = rowCityNew.getText().toString();
            progress = new ProgressDialog(RegActivity.this);
            progress.setMessage("Регистрируем вас ;)");
            progress.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                String query = "SELECT * FROM `users` WHERE `Login` = '" + login + "'";
                rs = statement.executeQuery(query);
                while(rs.next()){
                    return 1;
                }
                long date = System.currentTimeMillis() / 1000;
                String Token = getHash(password);
                query = "INSERT INTO `users` (`Login`, `Password`,`City`,`Name`,`Surname`,`DateRegistration`" +
                        ",`Token`,`phone`,`Age`) VALUES ('" + login + "', '" + password + "','" + city + "'," +
                        "'" + name + "','" + surname + "','" + date + "','" + Token + "', 'none', '" + age + "')";
                Log.e("Запрос", query);
                statement.executeUpdate(query);
                return 5;
            }
            catch (Exception e){
                Log.e("Reg", "Ошибка БД ", e);
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == 1)
            {
                progress.cancel();
                ErrorMessage("Введённый вами логин уже занят.\n\nНам очень жаль :(");
                return;
            }
            Intent intent = new Intent(RegActivity.this, AuthActivity.class);
            intent.putExtra("key", integer);
            finish();
            startActivity(intent);
        }
    }

    public String getHash(String str) {

        MessageDigest md5 ;
        StringBuffer  hexString = new StringBuffer();

        try {
            md5 = MessageDigest.getInstance("md5");

            md5.reset();
            md5.update(str.getBytes());

            byte messageDigest[] = md5.digest();

            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }

        }
        catch (NoSuchAlgorithmException e) {
            return e.toString();
        }

        return hexString.toString();
    }

    /*public boolean EmailValidator(String str){
        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                        "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(str);
        return matcher.matches();
    }*/

    public boolean LoginValidator(String str){
        Pattern pattern;
        Matcher matcher;
        String LOGIN_PATTERN = "^[A-Za-z0-9_-]{3,32}$";
        pattern = Pattern.compile(LOGIN_PATTERN);
        matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public boolean PasswordValidator(String str){
        Pattern pattern;
        Matcher matcher;
        String PASSWORD_PATTERN = "^[A-Za-z0-9_-]{6,16}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(str);
        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(RegActivity.this);
        builder.setTitle("Выход");
        builder.setMessage("Если вы покините регистрация, то все введёные вами данные будут потеряны.\n\n" +
                "Вы действительно хотите закрыть регистрацию?");
        builder.setCancelable(false);
        builder.setNegativeButton("Остаться", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Выйти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(RegActivity.this, AuthActivity.class);
                finish();
                startActivity(intent);
            }
        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void ErrorMessage(String message){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(RegActivity.this);
        builder.setTitle("Ошибка");
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}
