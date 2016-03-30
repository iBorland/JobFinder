package com.iborland.jobfinder;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

/**
 * Created by iborland on 20.03.16.
 */
public class Post implements Parcelable{

    int id;
    int ownerID;
    String ownerName;
    String postName;
    String postText;
    String cost;
    int status;
    int category;
    String createtime;
    boolean loaded = false;
    String[] Adresses;
    String[] Coords;
    int amount;

    int buffer_id;

    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;
    PostLoad loadPost;

    Post(int db_id){
        buffer_id = db_id;
        loadPost = new PostLoad();
        try {
            loadPost.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PostLoad extends Thread{

        PostLoad(){
            start();
        }

        public void run() {
            try{
                String query = "SELECT * FROM `posts` WHERE `id` = '" + buffer_id + "'";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()) {
                    id = rs.getInt("id");
                    ownerID = rs.getInt("ownerID");
                    ownerName = rs.getString("ownerName");
                    postName = rs.getString("postName");
                    postText = rs.getString("postText");
                    cost = rs.getString("cost");
                    status = rs.getInt("status");
                    category = rs.getInt("Category");
                    createtime = rs.getString("createtime");
                    String buffer_adresses = rs.getString("Adresses");
                    String buffer_coords = rs.getString("Coordinates");
                    amount = rs.getInt("Amount");
                    if(buffer_adresses != null) {
                        Adresses = buffer_adresses.split("split");
                        Coords = buffer_coords.split("split");
                    }

                    loaded = true;
                    Log.e("Loaded", "post " + postName + " был загружен");
                    break;
                }
            }
            catch (Exception e)
            {
                Log.e("ERROR", "Ошибка БД ", e);
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    // упаковываем объект в Parcel
    public void writeToParcel(Parcel parcel, int flags) {
        if(loaded == false) {
            Log.e("Parcel", "Ошибка упаковка Parcel. Post not loaded");
            return;
        }
        Log.e("Parcel", "Parcel упакован");
        parcel.writeInt(id);
        parcel.writeInt(ownerID);
        parcel.writeString(ownerName);
        parcel.writeString(postName);
        parcel.writeString(postText);
        parcel.writeString(cost);
        parcel.writeInt(status);
        parcel.writeInt(category);
        parcel.writeString(createtime);
        parcel.writeInt(amount);
        if(amount > 0)
        {
            parcel.writeStringArray(Adresses);
            parcel.writeStringArray(Coords);
        }

    }

    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        // распаковываем объект из Parcel
        public Post createFromParcel(Parcel in) {
            Log.d("Parcel", "createFromParcel");
            return new Post(in);
        }

        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private Post(Parcel parcel) {
        if(loaded == true) {
            Log.e("Parcel", "Ошибка загрузки из Parcel. Post has been loaded");
            return;
        }
        id = parcel.readInt();
        ownerID = parcel.readInt();
        ownerName = parcel.readString();
        postName = parcel.readString();
        postText = parcel.readString();
        cost = parcel.readString();
        status = parcel.readInt();
        category = parcel.readInt();
        createtime = parcel.readString();
        amount = parcel.readInt();
        if(amount > 0) {
            Adresses = new String[amount];
            Coords = new String[amount];
            parcel.readStringArray(Adresses);
            parcel.readStringArray(Coords);
        }

        Log.e("Parcel", "Post был распакован из Parcel");
        loaded = true;
    }

}
