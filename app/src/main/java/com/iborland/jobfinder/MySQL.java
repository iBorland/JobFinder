package com.iborland.jobfinder;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by iBorland on 22.04.2016.
 */
public class MySQL extends Thread{

    static final Integer ERROR_NONE = 1;
    static final Integer ERROR_DIFF_LENGTH = -1;
    static final Integer ERROR_NOT_CONNECTION = -2;
    static final Integer ERROR_EXECUTE = -3;


    Connection connect = null;
    Statement statement = null;
    ResultSet rs = null;
    String table = null;
    boolean autoclosed = false;

    boolean connected = false;

    MySQL(String tab, boolean closed){
        if(closed == true) autoclosed = true;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                    MainActivity.db_password);
            statement = connect.createStatement();
            connected = true;
            table = tab;
        } catch (Exception e) {
            e.printStackTrace();
            connected = false;
        }
    }

    public Integer insert(String[] key, Object[] amount){

        if(key.length != amount.length) return ERROR_DIFF_LENGTH;

        int l = key.length;

        String str1 = "INSERT INTO `" + table + "` (";
        String str2 = " VALUES (";

        for(int i = 0; i != l; i++){
            if(i == (l - 1)){
                String buffer = "`" + key[i] + "`)";
                str1 += buffer;
                buffer = "'" + amount[i] + "')";
                str2 += buffer;
                break;
            }
            String buffer = "`" + key[i] + "`, ";
            str1 += buffer;
            buffer = "'" + amount[i] + "', ";
            str2 += buffer;
        }

        String query = str1 + str2;
        Log.e("Query ", query);

        if(connect == null || statement == null) return ERROR_NOT_CONNECTION;

        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return ERROR_EXECUTE;
        }

        if(autoclosed == true) close();

        return ERROR_NONE;
    }

    public Integer update(String[] key, Object[] amount, int id) {

        if (key.length != amount.length) return ERROR_DIFF_LENGTH;

        int l = key.length;

        String string = "UPDATE `" + table  + "` SET ";

        for(int i = 0; i != l; i++){
            if(i == (l - 1)){
                String buffer = "`" + key[i] + "` = '" + amount[i] + "' WHERE `id` = '" + id + "'";
                string += buffer;
                break;
            }
            String buffer = "`" + key[i] + "` = '" + amount[i] + "', ";
            string += buffer;
        }

        Log.e("Query ", string);

        if(connect == null || statement == null) return ERROR_NOT_CONNECTION;

        try {
            statement.executeUpdate(string);
        } catch (SQLException e) {
            e.printStackTrace();
            return ERROR_EXECUTE;
        }

        if(autoclosed == true) close();

        return ERROR_NONE;
    }

    public Integer close(){
        try {
            connect.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connect = null;
        statement = null;
        return ERROR_NONE;
    }

}
