package com.iborland.jobfinder;

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
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final String db_login = "iborlZer";
    static final String db_ip = "triniti.ru-hoster.com/iborlZer?characterEncoding=utf8";
    static final String db_password = "22599226a";

    User user;
    String categories[];
    TextView message;
    RelativeLayout layout;
    ArrayList<Post> Posts = new ArrayList<Post>();

    FrameLayout f_Service, f_Postal, f_IT, f_Security, f_Repair, f_Other;

    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;
    Animation top;
    Animation left;
    Animation return_left;
    ScrollView scrollView;
    LinearLayout linearLayout;
    ActionBar act;
    Button create_post;
    boolean checkmail = false;

    int padding_in_dp = 10;
    int padding_in_px;
    BroadcastReceiver broadcastReceiver;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private boolean isReceiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final float scale = getResources().getDisplayMetrics().density;
        padding_in_px = (int) (padding_in_dp * scale + 0.5f);
        linearLayout = (LinearLayout)findViewById(R.id.linear);
        CheckLogin();
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        act = getSupportActionBar();
        top = AnimationUtils.loadAnimation(this, android.support.design.R.anim.abc_slide_in_top);
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);
        layout = (RelativeLayout)findViewById(R.id.Layout);
        categories = getResources().getStringArray(R.array.categories);
        message = (TextView)findViewById(R.id.message);
        f_Service = (FrameLayout)findViewById(R.id.f_Service);
        f_Postal = (FrameLayout)findViewById(R.id.f_Postal);
        f_IT = (FrameLayout)findViewById(R.id.f_IT);
        f_Security = (FrameLayout)findViewById(R.id.f_Security);
        f_Repair = (FrameLayout)findViewById(R.id.f_Repair);
        Intent Check_msg = new Intent(MainActivity.this, MessageService.class);
        startService(Check_msg);
        message.setText(getString(R.string.select_category));
        create_post = (Button)findViewById(R.id.button_create_post);
        //LoadMenu();

        create_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.loaded != true){
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
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkMail();
            }
        };
        IntentFilter intentFilter = new IntentFilter(User.USER_CREATED_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_messages){
            Toast.makeText(MainActivity.this, "В разработке", Toast.LENGTH_SHORT).show();
            /*if(user.loaded != true){
                Toast.makeText(MainActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent(MainActivity.this, DialogsActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);*/
        }
        if(id == R.id.nav_test){
            if(user.loaded != true){
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
        drawer.closeDrawer(GravityCompat.START);
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
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            finish();
            startActivity(intent);
        }
        else{
            Log.e("Authorition", "Пользователь авторизован");
            cursor.moveToFirst();
            user = new User(cursor.getInt(cursor.getColumnIndex("id")), cursor.getString(cursor.getColumnIndex("Token")), false, false, MainActivity.this);
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
        Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
        intent.putExtra("Category", category);
        intent.putExtra("User", user);
        startActivity(intent);
    }

    public void checkMail(){
        if(user.loaded == false) return;
        if(checkmail == true) return;
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
            text.setTextColor(getResources().getColor(R.color.colorBlackText));
            text.setTextSize(16);
            text.setGravity(Gravity.CENTER);
            linearLayout.addView(text, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
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
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
