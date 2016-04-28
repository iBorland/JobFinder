package com.iborland.jobfinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Inflater;

/**
 * Created by iBorland on 07.04.2016.
 */
public class CategoryActivity extends AppCompatActivity {

    ScrollView scroller;
    LinearLayout linearLayout;
    RelativeLayout relativeLayout;
    ProgressBar progressBar;

    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;
    Animation top;
    Animation left;
    Animation return_left;
    Menu buffer_menu;

    int select_category, amount = 0, count = 0;
    User user;
    ArrayList<Post> Posts = new ArrayList<Post>();
    LoadPosts loadPosts;
    ActionBar act;
    int padding_in_px;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        select_category = getIntent().getIntExtra("Category", -5);
        user = getIntent().getParcelableExtra("User");

        /*Intent intent = new Intent(CategoryActivity.this, MessageService.class);
        intent.putExtra("User", user);
        startService(intent);*/

        act = getSupportActionBar();

        int padding_in_dp = 8;  // 6 dps
        final float scale = getResources().getDisplayMetrics().density;
        padding_in_px = (int) (padding_in_dp * scale + 0.5f);

        progressBar = (ProgressBar)findViewById(R.id.progressBar2);
        scroller = (ScrollView)findViewById(R.id.scroller);
        linearLayout = (LinearLayout)findViewById(R.id.LinearInCat);
        relativeLayout = (RelativeLayout)findViewById(R.id.RelativeInCat);

        top = AnimationUtils.loadAnimation(this, android.support.design.R.anim.abc_slide_in_top);
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);

        if(select_category == -5){
            if(progressBar.isShown() == true) linearLayout.removeView(progressBar);
            TextView mess = new TextView(this);
            mess.setText(getString(R.string.error_loaded));
            mess.setGravity(Gravity.CENTER);
            mess.setTextColor(getResources().getColor(R.color.colorBlackText));
            mess.setTextSize(18);

            linearLayout.addView(mess);
            mess.startAnimation(top);
            return;
        }
        loadPosts = new LoadPosts();
        loadPosts.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if(buffer_menu == null) buffer_menu = menu;
        act.setHomeButtonEnabled(true);
        act.setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.item1){
            ClearPosts();
            if(loadPosts != null && loadPosts.getStatus() == AsyncTask.Status.RUNNING){
                Toast.makeText(CategoryActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
            loadPosts = new LoadPosts();
            loadPosts.execute();
        }
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    class LoadPosts extends AsyncTask<Void, Post, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Post... values) {
            super.onProgressUpdate(values);
            if(progressBar.isShown() == true) linearLayout.removeView(progressBar);
            AddPost(values[0], linearLayout);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(amount == 0){
                if(progressBar.isShown() == true) linearLayout.removeView(progressBar);
                TextView msg = new TextView(CategoryActivity.this);
                msg.setTextSize(16);
                msg.setTextColor(getResources().getColor(R.color.colorBlackText));
                msg.setText(getString(R.string.posts_not_found));
                msg.setGravity(Gravity.CENTER);
                msg.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
                linearLayout.addView(msg);

                TextView createAdd = new TextView(CategoryActivity.this);
                createAdd.setPadding(0, padding_in_px, 0, padding_in_px);
                createAdd.setTextSize(16);
                createAdd.setTextColor(getResources().getColor(R.color.colorBlackText));
                createAdd.setText(getString(R.string.create_post));
                createAdd.setGravity(Gravity.CENTER);
                createAdd.setBackground(getResources().getDrawable(R.color.buttonbackground));
                createAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CategoryActivity.this, AddActivity.class);
                        intent.putExtra("User", user);
                        finish();
                        startActivity(intent);
                    }
                });
                linearLayout.addView(createAdd);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if(isCancelled() == true){
                    amount = 0;
                    return null;
                }
                String query = "SELECT * FROM `posts` WHERE `Category` = '" + select_category + "' AND `City` = " +
                        "'" + user.city + "'";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()) {
                    amount++;
                    int id = rs.getInt("id");
                    Post post = new Post(id);
                    Posts.add(post);
                    publishProgress(Posts.get(amount - 1));
                }
            }
            catch (Exception e){
                Log.e("Error:", "Ошибка конекта к БД", e);
            }
            return null;
        }
    }

    public boolean ClearPosts(){
        if(amount > 0){
            linearLayout.removeAllViews();
            amount = 0;
            Posts.clear();
        }
        return true;
    }

    public boolean AddPost(Post post, LinearLayout lin){

        FrameLayout frame = new FrameLayout(getApplicationContext());
        frame.setId(count);
        frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loadPosts.getStatus() == AsyncTask.Status.RUNNING) loadPosts.cancel(true);
                Intent intent = new Intent(CategoryActivity.this, PostActivity.class);
                intent.putExtra("Post", Posts.get(v.getId()));
                intent.putExtra("User", user);
                startActivity(intent);
            }
        });

        count++;

        String text = "";
        if(post.postText.length() > 300) {
            text = post.postText.substring(0, 300);
            text += " ...";
        }
        else{
            text = post.postText;
        }

        TextView name = new TextView(getApplicationContext());
        name.setText(post.postName + "\n\n" + text + "\n\n");
        frame.addView(name);
        name.setGravity(Gravity.LEFT | Gravity.TOP);
        name.setTextColor(getResources().getColor(R.color.colorBlackText));
        name.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

        TextView author = new TextView(getApplicationContext());
        author.setText("Автор: " + post.ownerLogin);
        frame.addView(author);
        author.setTextColor(getResources().getColor(R.color.colorBlackText));
        author.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        author.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
        author.setTextSize(12);

        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yy 'в' HH:mm");
        long unix = (long) Integer.parseInt(post.createtime);
        Date data = new Date(unix*1000);
        TextView time = new TextView(getApplicationContext());
        time.setText("Дата публикации: " + date.format(data));
        frame.addView(time);
        time.setTextColor(getResources().getColor(R.color.colorBlackText));
        time.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        time.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
        time.setTextSize(12);

        //frame.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        frame.setBackgroundResource(R.drawable.shadow_back);

        lin.addView(frame);
        frame.startAnimation(left);

        return true;
    }

}
