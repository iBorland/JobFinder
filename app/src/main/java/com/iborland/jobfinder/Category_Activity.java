package com.iborland.jobfinder;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/*

Класс - активити для просмотра объявлений в выбранной категории.
В эту активити можно попасть из главного меню (MainActivity) если выбрать какую-либо категорию.
Здесь объявлений загружаются из удалённой базы и выподяться "порциями" по 50 штук.

 */

public class Category_Activity extends AppCompatActivity {

    User user;
    int select_category;
    LinearLayout linearLayout;
    LinkedList<Post> posts = new LinkedList();
    LoadPosts loadPosts;
    SwipeRefreshLayout swipe;
    ProgressBar bar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        linearLayout = (LinearLayout)findViewById(R.id.LinearInCat);
        user = getIntent().getParcelableExtra("User");
        select_category = getIntent().getIntExtra("Category", 0);
        swipe = (SwipeRefreshLayout)findViewById(R.id.cat_swipe);
        bar = new ProgressBar(Category_Activity.this);
        linearLayout.addView(bar);

        if(user == null || select_category == 0){
            TextView textView = new TextView(Category_Activity.this);
            textView.setText(getString(R.string.error_loaded));
            textView.setTextSize(18);
            textView.setTextColor(ContextCompat.getColor(Category_Activity.this, R.color.colorBlackText));
            int padding_in_px = (int) (10 * getResources().getDisplayMetrics().density + 0.5f);
            textView.setPadding(padding_in_px, padding_in_px * 2, padding_in_px, padding_in_px);
            linearLayout.addView(textView);
        }

        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(loadPosts.getStatus() != AsyncTask.Status.FINISHED){
                    Toast.makeText(Category_Activity.this, "Объявления ещё не загрузились", Toast.LENGTH_SHORT).show();
                }
                else{
                    posts = new LinkedList<>();
                    linearLayout.removeAllViews();
                    loadPosts = new LoadPosts();
                    loadPosts.execute();
                }
            }
        });

        loadPosts = new LoadPosts();
        loadPosts.execute();

    }

    class LoadPosts extends AsyncTask<Void, Post, Integer>{

        boolean nulled = false;

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Connection connection;
                Statement statement;
                ResultSet rs;
                String query;

                if(posts.size() == 0)
                    query = "SELECT id FROM posts WHERE Category = " + select_category + " AND status = 5 LIMIT 50";
                else
                    query = "SELECT id FROM posts WHERE Category = " + select_category + " AND status = 5 " +
                            "AND id > " + posts.getLast().id + " LIMIT 50";

                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()){
                    if(nulled) break;
                    int id = rs.getInt("id");
                    Post post = new Post(id);
                    posts.add(post);
                    publishProgress(posts.getLast());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
            return 1;
        }

        @Override
        protected void onProgressUpdate(final Post... values) {
            super.onProgressUpdate(values);
            if(bar.isShown()) linearLayout.removeView(bar);
            if(swipe.isRefreshing() == true) swipe.setRefreshing(false);
            int padding_in_px = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
            FrameLayout frame = new FrameLayout(getApplicationContext());
            frame.setId(values[0].id);
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Category_Activity.this, PostActivity.class);
                    intent.putExtra("Post", values[0].id);
                    intent.putExtra("User", user);
                    finish();
                    startActivity(intent);
                }
            });

            String text;
            if(values[0].postText.length() > 300) {
                text = values[0].postText.substring(0, 300);
                text += " ...";
            }
            else{
                text = values[0].postText;
            }

            TextView name = new TextView(getApplicationContext());
            name.setText(values[0].postName + "\n\n" + text + "\n\n");
            frame.addView(name);
            name.setGravity(Gravity.LEFT | Gravity.TOP);
            name.setTextColor(getResources().getColor(R.color.colorBlackText));
            name.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

            TextView author = new TextView(getApplicationContext());
            author.setText("Автор: " + values[0].ownerLogin);
            frame.addView(author);
            author.setTextColor(getResources().getColor(R.color.colorBlackText));
            author.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
            author.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
            author.setTextSize(12);

            SimpleDateFormat date = new SimpleDateFormat("dd.MM.yy 'в' HH:mm");
            long unix = (long) Integer.parseInt(values[0].createtime);
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

            if(values[0].deleted == 0){
                try {
                    linearLayout.addView(frame);
                    frame.startAnimation(AnimationUtils.loadAnimation(Category_Activity.this, R.anim.slide_left));
                } catch (Exception e) {
                    nulled = true;
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipe.setVisibility(View.VISIBLE);
            swipe.setRefreshing(false);
            swipe.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            swipe.setRefreshing(false);
            if(posts.size() == 0){
                TextView textView = new TextView(Category_Activity.this);
                textView.setText(getString(R.string.posts_not_found));
                textView.setTextSize(18);
                textView.setTextColor(ContextCompat.getColor(Category_Activity.this, R.color.colorBlackText));
                int padding_in_px = (int) (10 * getResources().getDisplayMetrics().density + 0.5f);
                textView.setPadding(padding_in_px, padding_in_px * 2, padding_in_px, padding_in_px);
                linearLayout.addView(textView);
                return;
            }

            if((posts.size() % 50) == 0) {
                Button button = new Button(Category_Activity.this);
                button.setText(getString(R.string.load_more));
                button.setTextSize(16);
                button.setTextColor(ContextCompat.getColor(Category_Activity.this, R.color.colorBlackText));
                button.setBackgroundColor(ContextCompat.getColor(Category_Activity.this, R.color.buttonbackground));
                button.setGravity(Gravity.CENTER);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LoadPosts loadPosts = new LoadPosts();
                        loadPosts.execute();
                    }
                });
            }

        }
    }

    @Override
    public void onBackPressed() {
        user = null;
        select_category = 0;
        linearLayout = null;
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                onBackPressed();
                break;
            }
            case R.id.item1:{
                if(loadPosts.getStatus() != AsyncTask.Status.FINISHED){
                    Toast.makeText(Category_Activity.this, "Объявления ещё не загрузились", Toast.LENGTH_SHORT).show();
                    break;
                }
                else{
                    posts = new LinkedList<>();
                    linearLayout.removeAllViews();
                    loadPosts = new LoadPosts();
                    loadPosts.execute();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
