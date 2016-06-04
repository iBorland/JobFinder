package com.iborland.jobfinder;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by iborland on 29.03.16.
 */

/*

Этот класс создан для упрощения отправки POST запроса и принятия ответа

 */
public class APILoader {

    String adress = null;
    SendAPI sendAPI;
    String request = null;

    APILoader(String adr){
        adress = adr;
    }

    public boolean addParams(String[] parametres, String[] keys){
        if(parametres.length != keys.length) return false;
        if(adress == null) return false;
        for(int i = 0; i != parametres.length; i++){
            if(i == 0){
                adress += "?" + parametres[i] + "=" + keys[i];
            }
            else{
                adress += "&" + parametres[i] + "=" + keys[i];
            }
        }
        return true;
    }

    public String execute(){
        if(adress == null) return null;
        if(sendAPI != null) return null;

        sendAPI = new SendAPI();

        try{
            sendAPI.join();
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        sendAPI = null;
        return request;
    }

    class SendAPI extends Thread{

        SendAPI(){ start(); }

        public void run() {
            try {
                String adr = adress.replaceAll(" ", "+");
                Log.e("ADRESS", "ADRESS: " + adr);
                URLConnection conn = new URL(adr).openConnection();
                conn.setRequestProperty("Accept-Charset", "UTF-8");

                String result = "";
                String line;
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = rd.readLine()) != null) {
                    result += line;
                }
                rd.close();

                request = result;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

}
