package com.iborland.jobfinder;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by iborland on 19.03.16.
 */
public class User implements Parcelable{

    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;
    String query;
    String buffer_Token;

    int id;
    String login;
    String password;
    String surname;
    String name;
    String email;
    int score;
    int status;
    int ad_amount;
    String regdata;
    String token;
    boolean loaded = false;

    User(int db_id, String db_buffer){
        if(db_id < 1){
            Log.e("Error", "Неверный ID");
            return;
        }
        if(loaded == true) {
            Log.e("Error", "User уже загружен");
            return;
        }
        buffer_Token = db_buffer;
        query = "SELECT * FROM `users` WHERE `id` = '" + db_id + "'";
        LoadUser loadUser = new LoadUser();
        loadUser.execute();
    }

    class LoadUser extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://db4free.net:3306/projectz", "iborland",
                        "22599226a");
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()) {
                    id = rs.getInt("id");
                    login = rs.getString("Login");
                    password = rs.getString("Password");
                    surname = rs.getString("Surname");
                    name = rs.getString("Name");
                    email = rs.getString("Email");
                    score = rs.getInt("Score");
                    status = rs.getInt("Status");
                    ad_amount = rs.getInt("AmountPosts");
                    regdata = rs.getString("DateRegistration");
                    token = rs.getString("Token");
                    if(!token.equals(buffer_Token))
                    {
                        Log.e("Error:", "Ошибка. Несоответствие токена");
                        return null;
                    }
                    loaded = true;
                    Log.e("Loaded", "User " + login + " был загружен");
                    break;
                }
            }
            catch (Exception e){
                Log.e("Error", "Ошибка загрузки БД ", e);
            }
            return null;
        }
    }

    public int describeContents() {
        return 0;
    }

    // упаковываем объект в Parcel
    public void writeToParcel(Parcel parcel, int flags) {
        if(loaded == false) {
            Log.e("Parcel", "Ошибка упаковка Parcel. User not loaded");
            return;
        }
        Log.e("Parcel", "Parcel упакован");
        parcel.writeInt(id);
        parcel.writeString(login);
        parcel.writeString(password);
        parcel.writeString(surname);
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeInt(score);
        parcel.writeInt(status);
        parcel.writeInt(ad_amount);
        parcel.writeString(regdata);
        parcel.writeString(token);

    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        // распаковываем объект из Parcel
        public User createFromParcel(Parcel in) {
            Log.d("Parcel", "createFromParcel");
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private User(Parcel parcel) {
        if(loaded == true) {
            Log.e("Parcel", "Ошибка загрузки из Parcel. User has been loaded");
            return;
        }
        id = parcel.readInt();
        login = parcel.readString();
        password = parcel.readString();
        surname = parcel.readString();
        name = parcel.readString();
        email = parcel.readString();
        score = parcel.readInt();
        status = parcel.readInt();
        ad_amount = parcel.readInt();
        regdata = parcel.readString();
        token = parcel.readString();
        Log.e("Parcel", "User " + login + "был распакован из Parcel");
        loaded = true;
    }

}
