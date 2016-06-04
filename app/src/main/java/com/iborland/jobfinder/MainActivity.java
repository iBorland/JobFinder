package com.iborland.jobfinder;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/*

Этот класс - основное активити приложения, главное меню.
Здесь проверяется авторизован-ли пользователь, привязан-ли у него номер сотового телефона или email,
здесь создаётся боковое меню навигации, здесь отображаются созданные объявления пользователя, здесь
отображается то объявление, которое пользователь выполняет.

 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    User user;
    String categories[];
    RelativeLayout layout;

    ScrollView scrollView;
    LinearLayout linearLayout;
    ActionBar act;
    boolean checkmail = false;
    ListView mail_list, actual_list, check_list;

    int padding_in_dp = 10;
    int padding_in_px;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    boolean posts_loaded = false;
    String token;
    BroadcastReceiver broadcastReceiver;
    BroadcastReceiver recipient_token;
    SwipeRefreshLayout mySwipeRefreshLayout;
    TextView your_posts, actual_posts;
    MyPosts myPosts;
    ActualPost actualPost;
    RelativeLayout actual_layour, posts_layout, check_layout;

    boolean user_loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final float scale = getResources().getDisplayMetrics().density;
        mySwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        padding_in_px = (int) (padding_in_dp * scale + 0.5f);
        linearLayout = (LinearLayout)findViewById(R.id.linear);
        actual_layour = (RelativeLayout)findViewById(R.id.actual_layout);
        posts_layout = (RelativeLayout)findViewById(R.id.posts_layout);
        check_layout = (RelativeLayout)findViewById(R.id.check_layout);
        mail_list = (ListView)findViewById(R.id.main_list);
        actual_list = (ListView)findViewById(R.id.actual_list);
        check_list = (ListView)findViewById(R.id.check_list);
        actual_posts = (TextView)findViewById(R.id.your_actual);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        act = getSupportActionBar();
        layout = (RelativeLayout)findViewById(R.id.Layout);
        categories = getResources().getStringArray(R.array.categories);
        your_posts = (TextView)findViewById(R.id.your_posts);
        linearLayout.removeView(actual_layour);
        linearLayout.removeView(posts_layout);
        linearLayout.removeView(check_layout);
        final TextView message;
        message = (TextView)findViewById(R.id.message);
        if (message != null) {
            message.setText(getString(R.string.select_category));
        }
        Button create_post;
        create_post = (Button)findViewById(R.id.button_create_post);
        //LoadMenu();

        recipient_token = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                token = intent.getStringExtra("Token");
            }
        };
        registerReceiver(recipient_token, new IntentFilter(getString(R.string.token_loaded)));

        assert create_post != null;
        create_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!user.loaded){
                    Toast.makeText(MainActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e("test", user.phone);
                if(user.phone.equals("none")){
                    Intent intent = new Intent(MainActivity.this, PhoneActivity.class);
                    intent.putExtra("User", user);
                    startActivity(intent);
                    return;
                }
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                intent.putExtra("User", user);
                startActivity(intent);
            }
        });

        int addresult = getIntent().getIntExtra("AddResult", -5);
        if(addresult != -5){
            if(addresult == 1){
                Snackbar.make(layout, getString(R.string.succesfull_created), Snackbar.LENGTH_LONG).show();
            }
            if(addresult == 0){
                Snackbar.make(layout, getString(R.string.error_created), Snackbar.LENGTH_LONG).show();
            }
        }

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checked();
                if (!posts_loaded)
                {
                    posts_loaded = true;
                    actualPost = new ActualPost();
                    actualPost.execute(intent.getIntExtra("id", -5));
                    myPosts = new MyPosts();
                    myPosts.execute(intent.getIntExtra("id", -5));
                    if(user == null) Log.e("USER", "USER = null");
                    if(user.msg_id == null || !user.msg_id.equals(token)){
                        if(user.msg_id != null) Log.e("MSG_ID", user.msg_id);
                        else Log.e("MSG_ID", "null");
                        if(token != null) Log.e("Token", token);
                        UpdateToken updateToken = new UpdateToken();
                        updateToken.execute();
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(User.USER_CREATED_ACTION));

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        mySwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boolean check = false;
                if(myPosts.getStatus() == AsyncTask.Status.FINISHED){
                    myPosts = new MyPosts();
                    myPosts.execute(user.id);
                    check = true;
                }
                if(actualPost.getStatus() == AsyncTask.Status.FINISHED){
                    actualPost = new ActualPost();
                    actualPost.execute(user.id);
                    check = true;
                }
                if(check == false) mySwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_messages){
            if(!user.loaded){
                Toast.makeText(MainActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent(MainActivity.this, DialogsActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        }
        if(id == R.id.nav_test){
            if(!user.loaded){
                Toast.makeText(MainActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent(MainActivity.this, VoteActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        }

        if (id == R.id.nav_main) {
            // Handle the camera action
        } else if (id == R.id.nav_profile) {
            if(!user.loaded){
                Toast.makeText(MainActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("User", user);
            intent.putExtra("Profile", user);
            startActivity(intent);

        }  else if (id == R.id.nav_exit) {
            DBHelper mDatabaseHelper;
            SQLiteDatabase mSqLiteDatabase;

            mDatabaseHelper = new DBHelper(this, "userinfo.db", null, 2);
            mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
            mDatabaseHelper.onUpgrade(mSqLiteDatabase, 2, 2);
            CheckLogin();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    public void CheckLogin()
    {
        if(!isOnline(MainActivity.this)){
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.error));
            builder.setMessage(getString(R.string.not_found_internet));
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });
            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();
        }
        DBHelper mDatabaseHelper;
        SQLiteDatabase mSqLiteDatabase;

        mDatabaseHelper = new DBHelper(this, "userinfo.db", null, 2);
        mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
        Cursor cursor = mSqLiteDatabase.query("user", new String[]{"id", "Token"},
                null, null,
                null, null, null) ;
        if(cursor.getCount() < 1){
            Log.e("Authorition", "Пользователь не авторизован. Открываю авторизацию");
            Intent intent = new Intent(MainActivity.this, Auth_Activity.class);
            finish();
            startActivity(intent);
        }
        else{
            Log.e("Authorition", "Пользователь авторизован");
            cursor.moveToFirst();
            if(user == null || user_loaded == true) {
                user_loaded = false;
                user = new User(cursor.getInt(cursor.getColumnIndex("id")), cursor.getString(cursor.getColumnIndex("Token")), false, false, MainActivity.this);
            }
        }
        cursor.close();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    @Override
    protected void onResume() {
        super.onResume();
        user_loaded = true;
        CheckLogin();
        registerReceiver();
        if(user != null) {
            actualPost = new ActualPost();
            actualPost.execute(user.id);
            myPosts = new MyPosts();
            myPosts.execute(user.id);
        }
    }

    public void SelectCategory(View view) {
        switch (view.getId()){
            case R.id.f_Service: openCategory(1); break;
            case R.id.f_Postal: openCategory(2); break;
            case R.id.f_IT: openCategory(3); break;
            case R.id.f_Security: openCategory(4); break;
            case R.id.f_Repair: openCategory(5); break;
            case R.id.f_Other: openCategory(6); break;
        }
    }

    public void openCategory(int category){
        Intent intent = new Intent(MainActivity.this, Category_Activity.class);
        intent.putExtra("Category", category);
        intent.putExtra("User", user);
        startActivity(intent);
    }

    boolean p = false, m = false;

    public void checked(){
        if(!user.loaded) return;
        if(checkmail) return;
        checkmail = true;
        ArrayList<String> checked_list = new ArrayList<>(2);
        if(user.phone.equals("none")) {
            checked_list.add(getString(R.string.accept_phone_number));
            p = true;
        }
        if(user.email.equals("none")){
            checked_list.add(getString(R.string.accept_mail));
            m = true;
        }
        if(checked_list.size() < 1) return;
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, checked_list);
        check_list.setAdapter(adapter);
        linearLayout.addView(check_layout, 0);
        setListViewHeightBasedOnChildren(check_list);
        check_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(check_list.getCount() == 2){
                    if(position == 0){
                        if(user.phone.equals("none")){
                            Intent intent = new Intent(MainActivity.this, PhoneActivity.class);
                            intent.putExtra("User", user);
                            startActivity(intent);
                            return;
                        }
                    }
                    if(position == 1){
                        Intent intent = new Intent(MainActivity.this, EmailActivity.class);
                        intent.putExtra("User", user);
                        finish();
                        startActivity(intent);
                        return;
                    }
                }
                if(p){
                    if(user.phone.equals("none")){
                        Intent intent = new Intent(MainActivity.this, PhoneActivity.class);
                        intent.putExtra("User", user);
                        startActivity(intent);
                        return;
                    }
                }
                else{
                    Intent intent = new Intent(MainActivity.this, EmailActivity.class);
                    intent.putExtra("User", user);
                    finish();
                    startActivity(intent);
                    return;
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(recipient_token);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("MainActivity", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    class MyPosts extends AsyncTask<Integer, Void, Integer>{

        ArrayList<Post> posts = new ArrayList<>(5);

        @Override
        protected Integer doInBackground(Integer... params) {
            if(user == null) return -1;
            int amount = 0;
            try{
                if(params[0] == 0) return -1;
                APILoader apiLoader = new APILoader("http://api.jobfinder.ru.com/myposts.php"); // это круто
                apiLoader.addParams(new String[]{"id"}, new String[]{"" + params[0]});
                String str = apiLoader.execute();
                JSONArray jsonArray = new JSONArray(str);
                for(int i = 0; i != jsonArray.length(); i++){
                    Post post = new Post(jsonArray.getInt(i));
                    posts.add(post);
                    amount++;
                }
            }
            catch (Exception e){
                e.printStackTrace();
                return -2;
            }
            return amount;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(posts_layout.isShown()){
                linearLayout.removeView(posts_layout);
            }
            mySwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mySwipeRefreshLayout.setRefreshing(false);
            if(integer <= 0) return;

            ArrayList<HashMap<String, Object>> data = new ArrayList<>(5);
            HashMap<String, Object> map;

            final String[] status = new String[] {"Ожидает модерации", "Отклонено: ", "Ожидает исполнителя", "Выполняет: ", "Исполнено"};
            final String[] from = new String[] {"Name", "Status", "Date", "Login", "Text"};
            final int[] to = new int[] {R.id.main_name, R.id.main_status, R.id.main_date, R.id.main_login, R.id.main_text};

            for(int i = 0; i != posts.size(); i++){
                map = new HashMap<>();
                if(posts.get(i).status == -1) map.put(from[1], status[1] + posts.get(i).reason);
                if(posts.get(i).status == 1) map.put(from[1], status[0]);
                if(posts.get(i).status == 5) map.put(from[1], status[2]);
                if(posts.get(i).status == 6) map.put(from[1], status[3] + posts.get(i).executor_name);
                if(posts.get(i).status == 10) map.put(from[1], status[4]);
                map.put(from[0], posts.get(i).postName);
                map.put(from[2], new SimpleDateFormat("dd.MM.yy 'в' HH:mm").format(new Date((long) Integer.parseInt(posts.get(i).createtime)*1000)));
                map.put(from[3], posts.get(i).ownerLogin);
                map.put(from[4], posts.get(i).postText);
                data.add(map);
            }

            SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, data, R.layout.list_main_posts, from, to);
            mail_list.setAdapter(adapter);

            if(posts_layout.isShown() == true) linearLayout.removeView(posts_layout);
            linearLayout.addView(posts_layout);
            setListViewHeightBasedOnChildren(mail_list);

            mail_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent post = new Intent(MainActivity.this, PostActivity.class);
                    post.putExtra("User", user);
                    post.putExtra("Post", posts.get(position).id);
                    startActivity(post);
                }
            });
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    class UpdateToken extends AsyncTask<Void, Void, Void>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getString(R.string.loaded));
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                user.msg_id = token;
                Connection connection;
                Statement statement;
                String query = "UPDATE `users` SET `msg_id` = '" + token + "' WHERE `id` = '" + user.id + "'";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                statement.executeUpdate(query);
                connection.close();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class ActualPost extends AsyncTask<Integer, Void, Integer>{

        int amount = 0;
        Post post;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(actual_layour.isShown()){
                linearLayout.removeView(actual_layour);
            }
            mySwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try{
                Connection connection;
                Statement statement;
                ResultSet rs;
                String query = "SELECT id FROM posts WHERE status = 6 AND executor = " + params[0];
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()){
                    post = new Post(rs.getInt("id"));
                    amount++;
                }
                if(amount <= 0) return -1;
                connection.close();
                statement.close();
                rs.close();
            }
            catch (Exception e){
                e.printStackTrace();
                return 0;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer != 1 || amount == 0) return;
            ArrayList<HashMap<String, Object>> data = new ArrayList<>(5);
            HashMap<String, Object> map;

            final String[] from = new String[] {"Name", "Status", "Date", "Login", "Text"};
            final int[] to = new int[] {R.id.main_name, R.id.main_status, R.id.main_date, R.id.main_login, R.id.main_text};

            for(int i = 0; i != amount; i++){
                map = new HashMap<>();
                map.put(from[1], "Выполняет: " + post.executor_name);
                map.put(from[0], post.postName);
                map.put(from[2], new SimpleDateFormat("dd.MM.yy 'в' HH:mm").format(new Date((long) Integer.parseInt(post.createtime)*1000)));
                map.put(from[3], post.ownerLogin);
                map.put(from[4], post.postText);
                data.add(map);
            }

            SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, data, R.layout.list_main_posts, from, to);
            actual_list.setAdapter(adapter);

            if(actual_layour.isShown() == true) linearLayout.removeView(actual_layour);
            try {
                linearLayout.addView(actual_layour);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setListViewHeightBasedOnChildren(mail_list);

            actual_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent postq = new Intent(MainActivity.this, PostActivity.class);
                    postq.putExtra("User", user);
                    postq.putExtra("Post", post.id);
                    startActivity(postq);
                }
            });
        }
    }


}
