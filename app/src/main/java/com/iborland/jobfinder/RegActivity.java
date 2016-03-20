package com.iborland.jobfinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
public class RegActivity extends Activity {

    String[] quests;
    int quest = 1;
    ArrayList<String> answers = new ArrayList<String>();
    TextView text;
    EditText etext;
    Button button;
    boolean button_showed;
    RelativeLayout rel;
    Animation top;
    Animation left;
    Animation return_left;

    Snackbar mSnackbar;
    View snackbarView;
    TextView snackTextView;
    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        quests = getResources().getStringArray(R.array.reg_quests);
        text = (TextView)findViewById(R.id.regText);
        etext = (EditText)findViewById(R.id.regRow);
        button = (Button)findViewById(R.id.regButton);
        rel = (RelativeLayout)findViewById(R.id.Lay);
        rel.removeView(button);
        button_showed = false;
        top = AnimationUtils.loadAnimation(this, android.support.design.R.anim.abc_slide_in_top);
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);
        mSnackbar = Snackbar.make(rel, "Слишком короткий логин", Snackbar.LENGTH_LONG);
        snackbarView = mSnackbar.getView();
        snackTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackTextView.setTextColor(getResources().getColor(R.color.colorText));

        text.setText(quests[quest-1]);

        text.startAnimation(top);
        etext.startAnimation(left);

        etext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 3 && button_showed == false) {
                    rel.addView(button);
                    button_showed = true;
                    button.startAnimation(left);
                }
                if (s.length() < 3 && button_showed == true) {
                    button.startAnimation(return_left);
                    rel.removeView(button);
                    button_showed = false;
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etext.length() < 3){
                    snackTextView.setText("Слишком короткий текст");
                    mSnackbar.show();
                    return;
                }
                if(etext.length() > 64){
                    snackTextView.setText("Слишком длинный текст");
                    mSnackbar.show();
                    return;
                }

                NextQuest();
            }
        });
    }

    public void NextQuest(){
        if(quest == 1)
        {
            if(!LoginValidator(etext.getText().toString())){
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(RegActivity.this);
                builder.setTitle("Ошибка");
                builder.setMessage("Ошибка ввода логина.\n\nЛогин может состоять только из символов" +
                        "латинского алфавита.\n\nДлинна логина не может быть меньше 3-ёх и более 16-и символов");
                builder.setCancelable(false);
                builder.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
                return;
            }
        }
        if(quest == 2)
        {
            if(!PasswordValidator(etext.getText().toString())){
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(RegActivity.this);
                builder.setTitle("Ошибка");
                builder.setMessage("Ошибка ввода пароля.\n\nПароль может состоять только из символов" +
                        "латинского алфавита.\n\nДлинна пароля не может быть меньше 6-и и более 16-и символов");
                builder.setCancelable(false);
                builder.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
                return;
            }
        }
        if(quest == 3)
        {
            if(!EmailValidator(etext.getText().toString())){
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(RegActivity.this);
                builder.setTitle("Ошибка");
                builder.setMessage("Ошибка ввода адреса электронной почты." +
                        "\n\nEmail может состоять только из символов" +
                        "латинского алфавита и обязательно должна содержать символ @.");
                builder.setCancelable(false);
                builder.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
                return;
            }
        }
        button.startAnimation(return_left);
        rel.removeView(button);
        button_showed = false;
        quest++;
        if(quest <= quests.length){
            answers.add(etext.getText().toString());
            etext.setText("");
            text.setText(quests[quest-1]);
        }
        else{
            answers.add(etext.getText().toString());
            InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(button.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            rel.removeAllViews();
            rel.addView(text);
            text.setText("Загрузка данных");
            CreateUser createUser = new CreateUser();
            createUser.execute();
        }
    }

    class CreateUser extends AsyncTask<Void, Void, Integer>
    {
        String name, surname, login, password, email;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            login = answers.get(0);
            password = answers.get(1);
            email = answers.get(2);
            name = answers.get(3);
            surname = answers.get(4);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://db4free.net:3306/projectz", "iborland",
                        "22599226a");
                statement = connection.createStatement();
                String query = "SELECT * FROM `users` WHERE `Login` = '" + login + "'";
                rs = statement.executeQuery(query);
                while(rs.next()){
                    return 1;
                }
                query = "SELECT * FROM `users` WHERE `Email` = '" + email + "'";
                rs = statement.executeQuery(query);
                while(rs.next()){
                    return 2;
                }
                long date = System.currentTimeMillis() / 1000;
                String Token = getHash(password);
                query = "INSERT INTO `users` (`Login`, `Password`,`Email`,`Name`,`Surname`,`DateRegistration`" +
                        ",`Token`) VALUES ('" + login + "', '" + password + "','" + email + "'," +
                        "'" + name + "','" + surname + "','" + date + "','" + Token + "')";
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

    public boolean EmailValidator(String str){
        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                        "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public boolean LoginValidator(String str){
        Pattern pattern;
        Matcher matcher;
        String LOGIN_PATTERN = "^[A-Za-z0-9_-]{3,16}$";
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
}
