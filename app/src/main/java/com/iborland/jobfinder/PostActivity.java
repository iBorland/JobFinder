package com.iborland.jobfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by iborland on 28.03.16.
 */
public class PostActivity extends AppCompatActivity {

    TextView sName, sText, sCost, sAdresses, sDate, zName, zCity;
    FrameLayout fText, fCost, fInfo;
    LinearLayout lin;
    Post Post;
    Animation top, left, return_left;
    int padding_in_dp = 10;
    int padding_in_px;
    ActionBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final float scale = getResources().getDisplayMetrics().density;
        padding_in_px = (int) (padding_in_dp * scale + 0.5f);

        bar = getSupportActionBar();

        lin = (LinearLayout)findViewById(R.id.LinearinPost);
        sName = (TextView)findViewById(R.id.sName);
        sText = (TextView)findViewById(R.id.sText);
        sCost = (TextView)findViewById(R.id.sCost);
        sDate = (TextView)findViewById(R.id.sDate);
        sAdresses = (TextView)findViewById(R.id.sAdresses);
        zName = (TextView)findViewById(R.id.zName);
        zCity = (TextView)findViewById(R.id.zCity);
        fText = (FrameLayout)findViewById(R.id.fText);
        fCost = (FrameLayout)findViewById(R.id.fCost);
        fInfo = (FrameLayout)findViewById(R.id.fInfo);

        lin.removeView(sAdresses);
        top = AnimationUtils.loadAnimation(this, android.support.design.R.anim.abc_slide_in_top);
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);

        try{
            Post = getIntent().getParcelableExtra("Post");
        }
        catch (Exception e){
            sName.setText(getString(R.string.error_loaded));
            lin.removeAllViews();
            lin.addView(sName);
            e.printStackTrace();
            return;
        }

        sText.setText(Post.postText);
        sName.setText(Post.postName);
        sCost.setText(Post.cost);
        zName.setText(Post.ownerName);
        zCity.setText(Post.city);

        fText.startAnimation(left);
        fCost.startAnimation(left);
        fInfo.startAnimation(left);

        bar.setTitle(Post.postName);
        bar.setSubtitle("Автор: " + Post.ownerLogin);
        bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.baryellow));

        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yy 'в' HH:mm");
        long unix = (long) Integer.parseInt(Post.createtime);
        Date data = new Date(unix*1000);

        sDate.setText("Создано " + date.format(data));

        if(Post.amount > 0){
            lin.addView(sAdresses);
            for(int i = 0; i != Post.amount; i++){

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
