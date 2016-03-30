package com.iborland.jobfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by iborland on 28.03.16.
 */
public class PostActivity extends AppCompatActivity {

    TextView sName, sText, sCost, sAdresses, sDate, ssText, ssCost;
    LinearLayout lin;
    Post Post;
    Animation top, left, return_left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        lin = (LinearLayout)findViewById(R.id.LinearinPost);
        sName = (TextView)findViewById(R.id.sName);
        sText = (TextView)findViewById(R.id.sText);
        sCost = (TextView)findViewById(R.id.sCost);
        sDate = (TextView)findViewById(R.id.sDate);
        sAdresses = (TextView)findViewById(R.id.sAdresses);
        ssText = (TextView)findViewById(R.id.ssText);
        ssCost = (TextView)findViewById(R.id.ssCost);
        lin.removeView(sAdresses);
        top = AnimationUtils.loadAnimation(this, android.support.design.R.anim.abc_slide_in_top);
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);

        try{
            Post = getIntent().getParcelableExtra("Post");
        }
        catch (Exception e){
            sName.setText("Ошибка загрузки данных");
            lin.removeAllViews();
            lin.addView(sName);
            e.printStackTrace();
            return;
        }

        sName.setText(Post.postName);
        sName.startAnimation(top);
        sText.setText(Post.postText);
        sText.startAnimation(left);
        sCost.setText(Post.cost);
        sCost.startAnimation(left);
        sAdresses.startAnimation(left);
        ssText.startAnimation(left);
        ssCost.startAnimation(left);

        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yy 'в' hh:mm");
        long unix = (long) Integer.parseInt(Post.createtime);
        Date data = new Date(unix*1000);

        sDate.setText("Объявление добавлено: " + date.format(data));
        sDate.setTextColor(getResources().getColor(R.color.colorBlackText));
        sDate.setTextSize(10);
        sCost.startAnimation(left);

        if(Post.amount > 0){
            lin.addView(sAdresses);
            for(int i = 0; i != Post.amount; i++){

                int padding_in_dp = 10;  // 6 dps
                final float scale = getResources().getDisplayMetrics().density;
                int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

                Button btn = new Button(this);
                btn.setText("Адрес №" + (i + 1) + ": " + Post.Adresses[i]);
                btn.setTextSize(12);
                btn.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
                btn.setBackgroundColor(getResources().getColor(R.color.buttonbackground));
                lin.addView(btn);
                btn.startAnimation(left);
                btn.setId(i);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String c = String.copyValueOf(Post.Coords[v.getId()].toCharArray(), 10, 35);
                        double lat = (double) Double.parseDouble(String.copyValueOf(c.toCharArray(), 0, 17));
                        double lng = (double) Double.parseDouble(String.copyValueOf(c.toCharArray(), 18, 17));
                        Intent intent = new Intent(PostActivity.this, MapsActivity.class);
                        intent.putExtra("Lat", lat);
                        intent.putExtra("Lng", lng);
                        intent.putExtra("Type", 5);
                        intent.putExtra("Adress", Post.Adresses[v.getId()]);
                        startActivity(intent);
                    }
                });
            }
        }

    }

}
