package com.iborland.jobfinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by iborland on 28.03.16.
 */
public class PostActivity extends AppCompatActivity {

    TextView sName, sText, sCost, sAdresses, sDate, zName, zCity, action_Header;
    ListView list;
    FrameLayout fText, fInfo;
    LinearLayout lin;
    Post Post;
    User user;
    Animation top, left, return_left;
    int padding_in_dp = 10;
    int padding_in_px;
    ActionBar bar;
    ProgressDialog dialog;

    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final float scale = getResources().getDisplayMetrics().density;
        padding_in_px = (int) (padding_in_dp * scale + 0.5f);

        bar = getSupportActionBar();

        user = getIntent().getParcelableExtra("User");

        lin = (LinearLayout)findViewById(R.id.LinearinPost);
        sName = (TextView)findViewById(R.id.sName);
        sText = (TextView)findViewById(R.id.sText);
        sCost = (TextView)findViewById(R.id.sCost);
        sDate = (TextView)findViewById(R.id.sDate);
        sAdresses = (TextView)findViewById(R.id.sAdresses);
        action_Header = (TextView)findViewById(R.id.action_header);
        list = (ListView)findViewById(R.id.action_list);
        zName = (TextView)findViewById(R.id.zName);
        zCity = (TextView)findViewById(R.id.zCity);
        fText = (FrameLayout)findViewById(R.id.fText);
        fInfo = (FrameLayout)findViewById(R.id.fInfo);

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

        sAdresses.startAnimation(left);
        action_Header.startAnimation(left);
        list.startAnimation(left);
        fText.startAnimation(left);
        fInfo.startAnimation(left);

        bar.setTitle(Post.postName);
        bar.setSubtitle("Автор: " + Post.ownerLogin);

        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yy 'в' HH:mm");
        long unix = (long) Integer.parseInt(Post.createtime);
        Date data = new Date(unix*1000);

        sDate.setText("Создано " + date.format(data));

        if(Post.amount > 0){
            //lin.addView(sAdresses);
            for(int i = 0; i != Post.amount; i++){

                Button btn = new Button(this);
                btn.setText("Адрес №" + (i + 1) + ": " + Post.Adresses[i]);
                btn.setTextSize(12);
                btn.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
                btn.setBackgroundColor(getResources().getColor(R.color.buttonbackground));
                lin.addView(btn, 4 + i);
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
        else{
            TextView btn = new TextView(this);
            btn.setText("Адреса не указаны");
            btn.setTextSize(12);
            btn.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
            btn.setBackgroundColor(getResources().getColor(R.color.buttonbackground));
            lin.addView(btn, 5);
            btn.startAnimation(left);
        }

        Object[] icons = {R.drawable.ic_speaker_notes_black_24dp, R.drawable.ic_check_black_24dp};

        ArrayList<HashMap<String, Object>> info = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> buffer;

        for(int i = 0; i != 2; i++) {
            buffer = new HashMap<String, Object>();
            if(i == 0) buffer.put("Name", getString(R.string.action_sending));
            else buffer.put("Name", getString(R.string.action_accept));
            buffer.put("Icon", icons[i]);
            info.add(buffer);
        }

        String[] from = {"Name", "Icon"};
        int[] to = {R.id.name, R.id.icon};

        SimpleAdapter adapter = new SimpleAdapter(this, info, R.layout.list_category, from, to);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) Toast.makeText(PostActivity.this, getString(R.string.developed), Toast.LENGTH_SHORT).show();
                if(position == 1){
                    CheckExecutor check = new CheckExecutor();
                    check.execute();
                }
            }
        });

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

    class CheckExecutor extends AsyncTask<Void, Void, Integer>{

        int amount = 0;

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String query = "SELECT * FROM `posts` WHERE `Status` = '1' AND `executor` = " +
                        "'" + user.id + "'";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()) amount++;

                if(amount >= 5) return -1;

                rs = null;

                query = "SELECT * FROM `posts` WHERE `id` = '" + Post.id + "'";
                rs = statement.executeQuery(query);
                while(rs.next()){
                    if(rs.getInt("executor") != 0) return -2;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return -5;
            }

            MySQL mysql = new MySQL("posts", true);
            mysql.update(new String[] {"executor"}, new Object[] {user.id}, Post.id);
            try{
                mysql.join(30000);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            user.score += 5;
            user.update(new String[] {User.DB_SCORE}, new Object[] {user.score});
            return 1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(PostActivity.this);
            dialog.setMessage(getString(R.string.loaded));
            dialog.show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            dialog.hide();
            if(integer == 1) sendMessage(PostActivity.this, getString(R.string.success), getString(R.string.post_executed));
            if(integer == -5) sendMessage(PostActivity.this, getString(R.string.error), getString(R.string.error_connection));
            if(integer == -1) sendMessage(PostActivity.this, getString(R.string.error), getString(R.string.post_error_too_mach_executing));
            if(integer == -2) sendMessage(PostActivity.this, getString(R.string.error), getString(R.string.post_occupied));
        }
    }

    public void sendMessage(Context context, String header, String message){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle(header);
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
