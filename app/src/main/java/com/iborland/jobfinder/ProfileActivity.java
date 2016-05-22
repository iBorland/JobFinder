package com.iborland.jobfinder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by iBorland on 22.05.2016.
 */
public class ProfileActivity extends AppCompatActivity {

    User user, profile;
    TextView name, status, score, city, age, posts_add, posts_execute, contact_mail, contact_phone;
    Button openMessage;
    ImageView oval;
    ActionBar act;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = getIntent().getParcelableExtra("User");
        name = (TextView) findViewById(R.id.info_Name);
        status = (TextView) findViewById(R.id.info_status);
        oval = (ImageView) findViewById(R.id.img_status);
        score = (TextView) findViewById(R.id.score_amount);
        city = (TextView) findViewById(R.id.info_city);
        age = (TextView) findViewById(R.id.info_age);
        posts_add = (TextView) findViewById(R.id.info_posts_add);
        posts_execute = (TextView) findViewById(R.id.info_posts_execute);
        contact_mail = (TextView) findViewById(R.id.info_contact_mail);
        contact_phone = (TextView) findViewById(R.id.info_contact_phone);
        openMessage = (Button) findViewById(R.id.info_openMessage);
        act = getSupportActionBar();
        act.setHomeButtonEnabled(true);
        act.setDisplayHomeAsUpEnabled(true);

        profile = getIntent().getParcelableExtra("Profile");

        name.setText(String.format("%s %s", profile.surname, profile.name));
        checkOnline();
        score.setText(String.format("%s", profile.score));
        city.setText(String.format("%s %s", getString(R.string.info_city), profile.city));
        age.setText(String.format("%s %d", getString(R.string.info_age), profile.age));
        posts_add.setText(String.format("%s %d", getString(R.string.info_posts_add), profile.ad_amount));
        posts_execute.setText(String.format("%s %d", getString(R.string.info_posts_execute), profile.ex_amount));
        if(profile.email.equals("none"))
            contact_mail.setText(String.format("%s %s", getString(R.string.iinfo_contact_mail), "Не указана"));
        else
            contact_mail.setText(String.format("%s %s", getString(R.string.iinfo_contact_mail), profile.email));
        if(profile.phone.equals("none"))
            contact_phone.setText(String.format("%s %s", getString(R.string.iinfo_contact_phone), "Не указан"));
        else
            contact_phone.setText(String.format("%s %s", getString(R.string.iinfo_contact_phone), profile.phone));

        openMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openChat = new Intent(ProfileActivity.this, ChatActivity.class);
                openChat.putExtra("User", user);
                openChat.putExtra("Partner_User", profile);
                startActivity(openChat);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("DefaultLocale")
    public void checkOnline(){
        long online = Long.parseLong(profile.lostOnline);
        long now = System.currentTimeMillis() / 1000;
        long diff = Math.abs(now - online);

        if(diff < 300){
            status.setText(getString(R.string.online));
            status.setTextColor(getResources().getColor(R.color.green_online));
            oval.setImageResource(R.drawable.online);
            return;
        }

        int minutes;
        for(minutes = 0; diff > 60; minutes++) diff -= 60;

        int hours;
        for(hours = 0; minutes > 60; hours++) minutes -= 60;

        Log.e("TIME", "TIME: " + System.currentTimeMillis() / 1000 + " - Minuts: " + minutes + " - Hours: " + hours);

        if(hours > 24){
            SimpleDateFormat date = new SimpleDateFormat("dd.MM.yy 'в' HH:mm");
            long unix = (long) Integer.parseInt(profile.lostOnline);
            Date data = new Date(unix*1000);
            status.setText(String.format("%s %s", getString(R.string.last_online), date.format(data)));
            status.setTextColor(getResources().getColor(R.color.colorBlackText));
            oval.setImageResource(R.drawable.offline);
            return;
        }

        if(hours > 0){
            if((hours % 10) == 1){
                status.setText(String.format("%s %d час назад", getString(R.string.last_online), hours));
                status.setTextColor(getResources().getColor(R.color.colorBlackText));
                oval.setImageResource(R.drawable.offline);
                return;
            }
            if((hours % 10) <= 4) {
                status.setText(String.format("%s %d часа назад", getString(R.string.last_online), hours));
                status.setTextColor(getResources().getColor(R.color.colorBlackText));
                oval.setImageResource(R.drawable.offline);
            }
            else{
                status.setText(String.format("%s %d часов назад", getString(R.string.last_online), hours));
                status.setTextColor(getResources().getColor(R.color.colorBlackText));
                oval.setImageResource(R.drawable.offline);
            }
            return;
        }

        if(minutes > 0){
            if((hours % 10) == 1){
                status.setText(String.format("%s %d минуту назад", getString(R.string.last_online), minutes));
                status.setTextColor(getResources().getColor(R.color.colorBlackText));
                oval.setImageResource(R.drawable.offline);
                return;
            }
            if((minutes % 10) <= 4) {
                status.setText(String.format("%s %d минуты назад", getString(R.string.last_online), minutes));
                status.setTextColor(getResources().getColor(R.color.colorBlackText));
                oval.setImageResource(R.drawable.offline);
            }
            else{
                status.setText(String.format("%s %d минут назад", getString(R.string.last_online), minutes));
                status.setTextColor(getResources().getColor(R.color.colorBlackText));
                oval.setImageResource(R.drawable.offline);
            }
            return;
        }
    }
}

