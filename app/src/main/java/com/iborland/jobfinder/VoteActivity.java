package com.iborland.jobfinder;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by iBorland on 26.04.2016.
 */
public class VoteActivity extends AppCompatActivity {

    User user;
    FrameLayout frame;
    TextView name, text, cost;
    ActionBar bar;
    ProgressBar progress;
    Animation show, hide;

    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    String b_name, b_text, b_cost;
    int b_id;

    boolean loaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        user = getIntent().getParcelableExtra("User");
        frame = (FrameLayout)findViewById(R.id.vote_frame);
        name = (TextView)findViewById(R.id.vote_name);
        text = (TextView)findViewById(R.id.vote_text);
        cost = (TextView)findViewById(R.id.vote_cost);

        progress = (ProgressBar)findViewById(R.id.vote_progress);
        bar = getSupportActionBar();
        show = AnimationUtils.loadAnimation(VoteActivity.this, R.anim.vote_show);
        hide = AnimationUtils.loadAnimation(VoteActivity.this, R.anim.vote_hide);

        frame.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        new UpdateBalance().execute();

        SelectVote select = new SelectVote();
        select.execute();
    }

    public void updatePost(){
        name.setText(b_name);
        text.setText(b_text);
        cost.setText("Оплата: " + b_cost);
        progress.setVisibility(View.GONE);
        frame.setVisibility(View.VISIBLE);
        frame.startAnimation(show);
        loaded = true;
    }

    public void vote(View view) {
        if(loaded != true) return;
        if(user.score < 20){
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(VoteActivity.this);
            builder.setTitle(getString(R.string.error));
            builder.setMessage(getString(R.string.not_money));
            builder.setCancelable(false);
            builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        if(view.getId() == R.id.vote_accept){
            Vote v = new Vote();
            v.execute("none");
        }
        if(view.getId() == R.id.vote_cancel){
            final EditText text = new EditText(VoteActivity.this);
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(VoteActivity.this);
            builder.setTitle("Модерация");
            builder.setMessage("Введите причину отклонения объявления");
            builder.setView(text);
            builder.setCancelable(false);
            builder.setPositiveButton("Далее", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Vote v = new Vote();
                    v.execute(text.getText().toString());
                    dialog.cancel();
                }
            });
            builder.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();
        }
        loaded = false;
        user.score -= 20;
        new UpdateBalance().execute();
    }

    class SelectVote extends AsyncTask<Void, Void, Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            frame.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == 1) updatePost();
            if(integer == 0) Toast.makeText(VoteActivity.this, "Ошибка соединений. Попробуйте позже.", Toast.LENGTH_SHORT).show();
            if(integer == -2){
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(VoteActivity.this);
                builder.setTitle(getString(R.string.error));
                builder.setMessage("На данный момент нету объявлений нуждающихся в модерации.\n\nПопробуйте чуть позже");
                builder.setCancelable(false);
                builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try{
                String query = "SELECT * FROM `posts` WHERE `status` = '1' AND `vote_id` = '0' ORDER BY RAND() LIMIT 1";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                boolean finded = false;
                while (rs.next()){
                    b_id = rs.getInt("id");
                    b_name = rs.getString("postName");
                    b_text = rs.getString("postText");
                    b_cost = rs.getString("cost");
                    finded = true;
                }
                if(finded == false){
                    return -2;
                }
                query = "UPDATE `posts` SET `vote_id` = '" + user.id + "' WHERE `id` = '" + b_id + "'";
                statement.executeUpdate(query);
                connection.close(); connection = null;
                rs.close(); rs = null;
            }
            catch (Exception e){
                e.printStackTrace();
                return 0;
            }
            return 1;
        }
    }

    class Vote extends AsyncTask<String, Void, Integer>{
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == 1) {
                frame.startAnimation(hide);
                loaded = false;
                SelectVote select = new SelectVote();
                select.execute();
            }
            if(integer == 0) Toast.makeText(VoteActivity.this, "Ошибка соединений. Попробуйте позже.", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            String reason = params[0];
            String query = "";
            if(reason.equals("none"))
                query = "UPDATE `posts` SET `status` = '5' WHERE `id` = '" + b_id + "'";
            else
                query = "UPDATE `posts` SET `status` = '-1', `reason` = '" + reason + "' WHERE `id` = '" + b_id + "'";
            try{
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                statement.executeUpdate(query);
                connection.close(); connection = null;
            }
            catch (Exception e){
                e.printStackTrace();
                return 0;
            }
            return 1;
        }
    }

    @Override
    protected void onDestroy() {

        if(b_id != 0){
            Clear c = new Clear();
            c.execute();
        }

        super.onDestroy();
    }

    class Clear extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            String query = "UPDATE `posts` SET `vote_id` = '0' WHERE `id` = '" + b_id + "'";
            try{
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                statement.executeUpdate(query);
                connection.close(); connection = null;
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vote_menu, menu);
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) finish();
        if(item.getItemId() == R.id.vote_info){
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(VoteActivity.this);
            builder.setTitle(getString(R.string.vote_information));
            builder.setMessage(getString(R.string.vote_info));
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
        return true;
    }

    class UpdateBalance extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar.setSubtitle("Баланс: " + user.score + " coins");
        }

        @Override
        protected Void doInBackground(Void... params) {
            String query = "UPDATE `users` SET `Score` = '" + user.score + "' WHERE `id` = '" + user.id + "'";
            try{
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                statement.executeUpdate(query);
                connection.close(); connection = null;
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
}
