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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

    TextView sName, sText, sCost, sAdresses, sDate, zName, zCity, action_Header, zName_e, z_City_e;
    ListView list;
    FrameLayout fText, fInfo, fExecutor;
    LinearLayout lin;
    int post_id;
    Post Post;
    User user;
    Animation top, left, return_left;
    int padding_in_dp = 10;
    int padding_in_px;
    ActionBar bar;
    ProgressDialog dialog;
    Button accept;
    String[] ratings;
    User executor;

    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;
    User partner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final float scale = getResources().getDisplayMetrics().density;
        padding_in_px = (int) (padding_in_dp * scale + 0.5f);
        ratings = getResources().getStringArray(R.array.ratings);

        bar = getSupportActionBar();

        user = getIntent().getParcelableExtra("User");

        lin = (LinearLayout)findViewById(R.id.LinearinPost);
        accept = (Button)findViewById(R.id.accept_post);
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
        zName_e = (TextView)findViewById(R.id.zName_e);
        z_City_e = (TextView)findViewById(R.id.zCity_e);
        fExecutor = (FrameLayout)findViewById(R.id.fInfo_Executor);

        top = AnimationUtils.loadAnimation(this, android.support.design.R.anim.abc_slide_in_top);
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);

        Log.e("ДИАЛОГ", "Показываю " + System.currentTimeMillis() / 1000);
        final ProgressDialog progressDialog = new ProgressDialog(PostActivity.this);
        progressDialog.setMessage(getString(R.string.loaded));
        progressDialog.show();

        try{
            post_id = getIntent().getIntExtra("Post", -5);
            if(post_id != 5) Post = new Post(post_id);

        }
        catch (Exception e){
            sName.setText(getString(R.string.error_loaded));
            lin.removeAllViews();
            lin.addView(sName);
            e.printStackTrace();
            progressDialog.dismiss();
            return;
        }
        Log.e("ДИАЛОГ", "Показал " + System.currentTimeMillis() / 1000);

        progressDialog.dismiss();
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

        fInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadUzver l = new LoadUzver();
                l.execute();
            }
        });

        if(Post.status == 6 || Post.status == 10){
            LoadExecutor loadExecutor = new LoadExecutor();
            loadExecutor.execute();
        }

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
            lin.addView(btn, 4);
            btn.startAnimation(left);
        }

        Object[] icons = {R.drawable.ic_speaker_notes_black_24dp, R.drawable.ic_check_black_24dp};

        ArrayList<HashMap<String, Object>> info = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> buffer;

        for(int i = 0; i != 2; i++) {
            buffer = new HashMap<>();
            if(i == 0) buffer.put("Name", getString(R.string.action_sending));
            else buffer.put("Name", getString(R.string.action_accept));
            buffer.put("Icon", icons[i]);
            info.add(buffer);
        }

        if(user.id == Post.ownerID){
            buffer = new HashMap<>();
            buffer.put("Name", getString(R.string.delete));
            buffer.put("Icon", R.drawable.delete);
            info.add(buffer);
        }

        String[] from = {"Name", "Icon"};
        int[] to = {R.id.name, R.id.icon};

        SimpleAdapter adapter = new SimpleAdapter(this, info, R.layout.list_category, from, to);
        list.setAdapter(adapter);

        MainActivity.setListViewHeightBasedOnChildren(list);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    Intent chat = new Intent(PostActivity.this, ChatActivity.class);
                    chat.putExtra("User", user);
                    chat.putExtra("Partner", Post.ownerID);
                    startActivity(chat);
                    return;
                }
                if(position == 1){
                    CheckExecutor check = new CheckExecutor();
                    check.execute();
                }
                if(position == 2){
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(PostActivity.this);
                    builder.setTitle(getString(R.string.deleted));
                    builder.setMessage(getString(R.string.delete_accept));
                    builder.setCancelable(true);
                    builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            DeletePost delete = new DeletePost();
                            delete.execute();
                        }
                    });
                    builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        if(Post.status == 6) {
            if (user.id == Post.executor) { // если исполнитель
                if (Post.accept_post[0] == 0) {
                    accept.setVisibility(View.VISIBLE);
                    accept.startAnimation(left);
                } else {
                    accept.setVisibility(View.VISIBLE);
                    accept.startAnimation(left);
                    accept.setEnabled(false);
                    accept.setText(getString(R.string.accept_owner));
                }
            }
            if(user.id == Post.ownerID){ // если заказчик
                if (Post.accept_post[1] == 0) {
                    accept.setVisibility(View.VISIBLE);
                    accept.startAnimation(left);
                } else {
                    accept.setVisibility(View.VISIBLE);
                    accept.startAnimation(left);
                    accept.setEnabled(false);
                    accept.setText(getString(R.string.accept_executor));
                }
            }
        }

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.id == Post.executor){
                    Post.accept_post[0] = 1;
                    AnyThread anyThread = new AnyThread();
                    anyThread.execute(0);
                    accept.startAnimation(left);
                    accept.setEnabled(false);
                    accept.setText(getString(R.string.accept_owner));
                    return;
                }
                if(user.id == Post.ownerID){
                    if(Post.accept_post[0] == 1){
                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(PostActivity.this);
                        builder.setTitle(getString(R.string.rating_text));
                        builder.setCancelable(false);
                        builder.setItems(ratings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                FinishPost finishPost = new FinishPost();
                                finishPost.execute(which);
                            }
                        });
                        android.support.v7.app.AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else {
                        long diff = Math.abs(System.currentTimeMillis() / 1000 - Long.parseLong(Post.execute_start));
                        if (diff > 86400) {
                            Post.accept_post[1] = 1;
                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(PostActivity.this);
                            builder.setTitle(getString(R.string.rating_text));
                            builder.setCancelable(false);
                            builder.setItems(ratings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    FinishPost finishPost = new FinishPost();
                                    finishPost.execute(which);
                                }
                            });
                            android.support.v7.app.AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        else{
                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(PostActivity.this);
                            builder.setTitle(getString(R.string.error));
                            builder.setCancelable(false);
                            builder.setMessage(getString(R.string.error_finish));
                            builder.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                         dialog.cancel();
                                }
                            });
                            android.support.v7.app.AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                    return;
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
                if(Post.ownerID == user.id) return -3;
                String query = "SELECT * FROM `posts` WHERE `Status` = '6' AND `executor` = " +
                        "'" + user.id + "'";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()) amount++;

                if(amount >= 1) return -1;

                rs = null;

                query = "SELECT * FROM `posts` WHERE `id` = '" + Post.id + "'";
                rs = statement.executeQuery(query);
                while(rs.next()){
                    if(rs.getInt("executor") != 0) return -2;
                }
                if(Post.status != 5) return -2;

            } catch (Exception e) {
                e.printStackTrace();
                return -5;
            }

            Post.status = 6;
            Post.execute_start = "" + System.currentTimeMillis() / 1000;
            Post.executor_name = user.login;
            MySQL mysql = new MySQL("posts", true);
            mysql.update(new String[] {"executor", "executor_name", "status", "execute_start"}, new Object[] {user.id, Post.executor_name,  Post.status, Post.execute_start}, Post.id);
            try{
                mysql.join(30000);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            User partner = new User(Post.ownerID, "123", true, true, PostActivity.this);

            APILoader gcm = new APILoader("http://api.jobfinder.ru.com/gcm.php");
            gcm.addParams(new String[] {"regID", "type", "sender_name"},
                    new String[] {partner.msg_id, "2", user.login});
            gcm.execute();
            gcm = null;

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
            if(integer == -3) sendMessage(PostActivity.this, getString(R.string.error), getString(R.string.can_not));
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

    class DeletePost extends AsyncTask<Void, Void, Integer>{

        ProgressDialog dialog;

        @Override
        protected Integer doInBackground(Void... params) {
            try{
                Connection connection;
                Statement statement;
                String query = "UPDATE `posts` SET `deleted` = '1' WHERE `id` = '" + Post.id + "'";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                statement.executeUpdate(query);
                connection.close();
                statement.close();
            }
            catch (Exception e){
                e.printStackTrace();
                return 0;
            }
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
            dialog.dismiss();
            if(integer == 1) Toast.makeText(PostActivity.this, "Удалено", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(PostActivity.this, Category_Activity.class);
            i.putExtra("User",user);
            i.putExtra("Category", Post.category);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(i);
        }
    }

    class AnyThread extends AsyncTask<Integer, Void, Void>{
        @Override
        protected Void doInBackground(Integer... params) {
            if(params[0] == 0){
                MySQL m = new MySQL("posts", true);
                m.update(new String[] {"accept_executor"}, new Object[] {1}, Post.id);
                User owner = new User(Post.ownerID, "123", true, true, PostActivity.this);
                APILoader gcm = new APILoader("http://api.jobfinder.ru.com/gcm.php");
                gcm.addParams(new String[] {"regID", "type", "executor_name"},
                        new String[] {owner.msg_id, "3", user.login});
                gcm.execute();
                gcm = null;
            }
            if(params[0] == 1){
                MySQL m = new MySQL("posts", true);
                m.update(new String[] {"accept_owner"}, new Object[] {1}, Post.id);
            }
            return null;
        }
    }

    class FinishPost extends AsyncTask<Integer, Void, Integer>{

        ProgressDialog progressDialog;

        @Override
        protected Integer doInBackground(Integer... params) {
            User executor = new User(Post.executor, "123", true, true, PostActivity.this);
            int change_status = 0;
            if(params[0] == 0) change_status = 50;
            if(params[0] == 1) change_status = 25;
            if(params[0] == 2) change_status = 0;
            if(params[0] == 3) change_status = -25;
            if(params[0] == 4) change_status = -50;
            executor.score += change_status;
            if(executor.score < 0) executor.score = 0;
            executor.ex_amount++;
            APILoader gcm = new APILoader("http://api.jobfinder.ru.com/gcm.php");
            gcm.addParams(new String[] {"regID", "type"},
                    new String[] {executor.msg_id, "4"});
            gcm.execute();
            gcm = null;
            executor.update(new String[] {"Score", "AmountExecuted"}, new Object[] {executor.score, executor.ex_amount});

            Post.status = 10;
            MySQL m = new MySQL("posts", true);
            m.update(new String[] {"status"}, new Object[] {Post.status}, Post.id);

            return 1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PostActivity.this);
            progressDialog.setMessage(getString(R.string.loaded));
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progressDialog.dismiss();
            Intent intent = new Intent(PostActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        }
    }

    class LoadUzver extends AsyncTask<Void, Void, Void>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PostActivity.this);
            progressDialog.setMessage(getString(R.string.loaded));
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Intent userInfo = new Intent(PostActivity.this, ProfileActivity.class);
            userInfo.putExtra("User", user);
            userInfo.putExtra("Profile", partner);
            startActivity(userInfo);
        }

        @Override
        protected Void doInBackground(Void... params) {
            partner = new User(Post.ownerID, "123", true, true, PostActivity.this);
            return null;
        }
    }

    class LoadExecutor extends AsyncTask<Void, Void, Void>{

        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Void... params) {
            executor = new User(Post.executor, "123", true, true, PostActivity.this);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PostActivity.this);
            progressDialog.setMessage(getString(R.string.loaded));
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            zName_e.setText(executor.name + " " + executor.surname);
            z_City_e.setText(executor.city);
            fExecutor.setVisibility(View.VISIBLE);
            fExecutor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PostActivity.this, ProfileActivity.class);
                    intent.putExtra("User", user);
                    intent.putExtra("Profile", executor);
                    startActivity(intent);
                }
            });
        }
    }
}
