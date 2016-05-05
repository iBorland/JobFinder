package com.iborland.jobfinder;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
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

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    User user;
    String categories[];
    RelativeLayout layout;

    ScrollView scrollView;
    LinearLayout linearLayout;
    ActionBar act;
    boolean checkmail = false;
    ListView mail_list;

    int padding_in_dp = 10;
    int padding_in_px;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    boolean posts_loaded = false;
    String token;
    BroadcastReceiver broadcastReceiver;
    BroadcastReceiver recipient_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final float scale = getResources().getDisplayMetrics().density;
        padding_in_px = (int) (padding_in_dp * scale + 0.5f);
        linearLayout = (LinearLayout)findViewById(R.id.linear);
        mail_list = (ListView)findViewById(R.id.main_list);
        linearLayout.removeView(mail_list);
        CheckLogin();
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        act = getSupportActionBar();
        layout = (RelativeLayout)findViewById(R.id.Layout);
        categories = getResources().getStringArray(R.array.categories);
        TextView message;
        message = (TextView)findViewById(R.id.message);
        if (message != null) {
            message.setText(getString(R.string.select_category));
        }
        Button create_post;
        create_post = (Button)findViewById(R.id.button_create_post);
        //LoadMenu();

        String test = "sms/2/Текст";

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
                checkMail();
                if (!posts_loaded)
                {
                    posts_loaded = true;
                    MyPosts m = new MyPosts();
                    m.execute(intent.getIntExtra("id", -5));
                    if(user == null) Log.e("USER", "USER = null");
                    if(user.msg_id == null || !user.msg_id.equals(token)){
                        if(user.msg_id != null) Log.e("MSG_ID", user.msg_id);
                        else Log.e("MSG_ID", "null");
                        Log.e("Token", token);
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
            if(user == null)
                user = new User(cursor.getInt(cursor.getColumnIndex("id")), cursor.getString(cursor.getColumnIndex("Token")), false, false, MainActivity.this);
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
        CheckLogin();
        registerReceiver();
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

    public void checkMail(){
        if(!user.loaded) return;
        if(checkmail) return;
        if(user.email.equals("null")){
            checkmail = true;
            Button btn = new Button(MainActivity.this);
            btn.setText(getString(R.string.mail_created));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, EmailActivity.class);
                    intent.putExtra("User", user);
                    finish();
                    startActivity(intent);
                }
            });
            linearLayout.addView(btn, 0);

            TextView text = new TextView(MainActivity.this);
            text.setText(getString(R.string.mail_not_found));
            text.setTextColor(ContextCompat.getColor(MainActivity.this ,R.color.colorBlackText));
            text.setTextSize(16);
            text.setGravity(Gravity.CENTER);
            linearLayout.addView(text, 0);
        }
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

        ProgressBar bar = new ProgressBar(MainActivity.this);
        ArrayList<Post> posts = new ArrayList<>(5);

        @Override
        protected Integer doInBackground(Integer... params) {
            if(user == null) return -1;
            int amount = 0;
            try{
                Connection connection;
                Statement statement;
                ResultSet rs;
                String query = "SELECT * FROM `posts` WHERE `ownerID` = '" + params[0] + "' ORDER BY `id` DESC LIMIT 5";
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + getString(R.string.db_ip), getString(R.string.db_login),
                        getString(R.string.db_password));
                statement = connection.createStatement();
                rs = statement.executeQuery(query);
                while (rs.next()){
                    int id = rs.getInt("id");
                    Post post = new Post(id);
                    posts.add(post);
                    amount++;
                }
                connection.close();
                statement.close();
                rs.close();
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
            linearLayout.addView(bar);
            bar.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            linearLayout.removeView(bar);
            if(integer <= 0) return;

            ArrayList<HashMap<String, Object>> data = new ArrayList<>(5);
            HashMap<String, Object> map;

            final String[] status = new String[] {"Ожидает модерации", "Отклонено: ", "Ожидает исполнителя"};
            final String[] from = new String[] {"Name", "Status", "Date", "Login", "Text"};
            final int[] to = new int[] {R.id.main_name, R.id.main_status, R.id.main_date, R.id.main_login, R.id.main_text};

            for(int i = 0; i != posts.size(); i++){
                map = new HashMap<>();
                if(posts.get(i).status == -1) map.put(from[1], status[1] + posts.get(i).reason);
                if(posts.get(i).status == 1) map.put(from[1], status[0]);
                if(posts.get(i).status == 5) map.put(from[1], status[2]);
                map.put(from[0], posts.get(i).postName);
                map.put(from[2], new SimpleDateFormat("dd.MM.yy 'в' HH:mm").format(new Date((long) Integer.parseInt(posts.get(i).createtime)*1000)));
                map.put(from[3], posts.get(i).ownerLogin);
                map.put(from[4], posts.get(i).postText);
                data.add(map);
            }

            SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, data, R.layout.list_main_posts, from, to);
            mail_list.setAdapter(adapter);

            TextView text = new TextView(MainActivity.this);
            text.setText("Ваши объявления:");
            text.setTextSize(22);
            text.setTextColor(getResources().getColor(R.color.colorBlackText));
            text.setPadding(padding_in_px * 2, padding_in_px * 3, padding_in_px, padding_in_px);

            linearLayout.addView(text);
            linearLayout.addView(mail_list);
            setListViewHeightBasedOnChildren(mail_list);

            mail_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent post = new Intent(MainActivity.this, PostActivity.class);
                    post.putExtra("User", user);
                    post.putExtra("Post", posts.get(position));
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


}
