package com.iborland.jobfinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final String db_login = "user17667";
    static final String db_ip = "62.76.74.169:3306/user17667?characterEncoding=utf8";
    static final String db_password = "22599226a";

    User user;
    ListView listView;
    String categories[];
    TextView message;
    int select_category = 0;
    RelativeLayout layout;
    ArrayList<Post> Posts = new ArrayList<Post>();

    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;
    Animation top;
    Animation left;
    Animation return_left;
    ScrollView scrollView;
    LinearLayout linearLayout;
    Menu men;
    LoadCategory loadCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckLogin();
        setContentView(R.layout.activity_main);
        linearLayout = (LinearLayout)findViewById(R.id.linear);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        top = AnimationUtils.loadAnimation(this, android.support.design.R.anim.abc_slide_in_top);
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);
        layout = (RelativeLayout)findViewById(R.id.Layout);
        categories = getResources().getStringArray(R.array.categories);
        listView = (ListView)findViewById(R.id.listView);
        message = (TextView)findViewById(R.id.message);
        LoadMenu();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(user.loaded != true){
                        Toast.makeText(MainActivity.this, "Попробуйте через несколько секунд", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(MainActivity.this, AddActivity.class);
                    intent.putExtra("User", user);
                    startActivity(intent);
                }
            });
        }

        int addresult = getIntent().getIntExtra("AddResult", -5);
        if(addresult != -5){
            if(addresult == 1){
                Snackbar.make(layout, "Ваше объявление успешно создано", Snackbar.LENGTH_LONG).show();
            }
            if(addresult == 0){
                Snackbar.make(layout, "Ошибка. Ваше объявление не было создано.", Snackbar.LENGTH_LONG).show();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if(select_category != 0)
        {
            LoadMenu();
            men.setGroupVisible(R.id.group, false);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if(men == null) men = menu;
        men.setGroupVisible(R.id.group, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.item1)
        {
            if(loadCategory.getStatus() != AsyncTask.Status.FINISHED){
                Toast.makeText(MainActivity.this, "Обновление ещё не завершено", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
            men.setGroupVisible(R.id.group, false);
            loadCategory = new LoadCategory();
            loadCategory.execute();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            // Handle the camera action
        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_exit) {
            DBHelper mDatabaseHelper;
            SQLiteDatabase mSqLiteDatabase;

            mDatabaseHelper = new DBHelper(this, "userinfo.db", null, 2);
            mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
            mDatabaseHelper.onUpgrade(mSqLiteDatabase, 2, 2);
            CheckLogin();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void CheckLogin()
    {
        DBHelper mDatabaseHelper;
        SQLiteDatabase mSqLiteDatabase;

        mDatabaseHelper = new DBHelper(this, "userinfo.db", null, 2);
        mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
        Cursor cursor = mSqLiteDatabase.query("user", new String[]{"id", "Token"},
                null, null,
                null, null, null) ;
        if(cursor.getCount() < 1){
            Log.e("Authorition", "Пользователь не авторизован. Открываю авторизацию");
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            finish();
            startActivity(intent);
        }
        else{
            Log.e("Authorition", "Пользователь авторизован");
            cursor.moveToFirst();
            user = new User(cursor.getInt(cursor.getColumnIndex("id")), cursor.getString(cursor.getColumnIndex("Token")));
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void LoadMenu(){
        Posts.clear();
        layout.setBackgroundColor(getResources().getColor(R.color.white));
        Log.e("CLEAR", "Посты очищены");
        int padding_in_dp = 16;  // 6 dps
        final float scale = getResources().getDisplayMetrics().density;
        int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
        layout.setPadding(padding_in_px,padding_in_px,padding_in_px,padding_in_px);
        layout.removeAllViews();
        layout.addView(message);
        layout.addView(listView);
        message.setText("Выберите категорию:");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                select_category = position + 1;
                loadCategory = new LoadCategory();
                loadCategory.execute();
            }
        });
    }



    class LoadCategory extends AsyncTask<Void, Void, Void>{

        int amount = 0;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String query = "SELECT * FROM `posts` WHERE `Category` = '" + select_category + "'";
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
                }
            }
            catch (Exception e){
                Log.e("Error:", "Ошибка конекта к БД", e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            layout.removeAllViews();
            layout.addView(message);
            message.setText("Загрузка постов");
            message.startAnimation(top);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            linearLayout.removeAllViews();
            if(amount == 0)
            {
                int padding_in_dp = 16;  // 6 dps
                final float scale = getResources().getDisplayMetrics().density;
                int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
                layout.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
                message.setText("В данной категории пока-что нету объявлений.\n\nНам очень жаль.");
                return;
            }
            layout.setBackgroundColor(getResources().getColor(R.color.materialBackground));
            men.setGroupVisible(R.id.group, true);
            for(int i = 0; i != Posts.size(); i++){


                int padding_in_dp = 8;  // 6 dps
                final float scale = getResources().getDisplayMetrics().density;
                int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

                FrameLayout frame = new FrameLayout(getApplicationContext());
                frame.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                frame.setId(Posts.get(i).id);

                String text = "";
                if(Posts.get(i).postText.length() > 300) {
                    text = Posts.get(i).postText.substring(0, 300);
                    text += " ...";
                }
                else{
                    text = Posts.get(i).postText;
                }

                TextView name = new TextView(getApplicationContext());
                name.setText(Posts.get(i).postName + "\n\n" + text + "\n\n");
                frame.addView(name);
                name.setGravity(Gravity.LEFT | Gravity.TOP);
                name.setTextColor(getResources().getColor(R.color.colorBlackText));
                name.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

                TextView author = new TextView(getApplicationContext());
                author.setText("Автор: " + Posts.get(i).ownerName);
                frame.addView(author);
                author.setTextColor(getResources().getColor(R.color.colorBlackText));
                author.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
                author.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
                author.setTextSize(12);

                SimpleDateFormat date = new SimpleDateFormat("dd.MM.yy 'в' hh:mm");
                long unix = (long) Integer.parseInt(Posts.get(i).createtime);
                Date data = new Date(unix*1000);
                TextView time = new TextView(getApplicationContext());
                time.setText("Дата публикации: " + date.format(data));
                frame.addView(time);
                time.setTextColor(getResources().getColor(R.color.colorBlackText));
                time.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                time.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
                time.setTextSize(12);

                linearLayout.addView(frame);
            }
            layout.setPadding(0, 0, 0 ,0);
            layout.removeView(message);
            layout.addView(scrollView);
            scrollView.startAnimation(left);
        }
    }

}
