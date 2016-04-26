package com.iborland.jobfinder;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by iborland on 25.03.16.
 */
public class AddActivity extends AppCompatActivity {

    EditText pName, pText, pCost, pCity;
    TextView pCategory;
    LinearLayout ll;
    Button button;
    String[] categories;
    int amount = 0, category = 0;
    ArrayList<String> coords = new ArrayList<String>();
    ArrayList<String> adress = new ArrayList<String>();
    Animation left, return_left, top, return_top;
    User user;

    Connection connection = null;
    Statement statement = null;

    Snackbar mSnackbar;
    View snackbarView;
    TextView snackTextView;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        ll = (LinearLayout)findViewById(R.id.LinearInScroll);
        user = getIntent().getParcelableExtra("User");
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);
        top = AnimationUtils.loadAnimation(this, R.anim.slide_top);
        return_top = AnimationUtils.loadAnimation(this, R.anim.return_slide_top);
        categories = getResources().getStringArray(R.array.categories);
        pName = (EditText)findViewById(R.id.pName);
        pText = (EditText)findViewById(R.id.pText);
        pCost = (EditText)findViewById(R.id.pCost);
        pCategory = (TextView)findViewById(R.id.pCategory);
        pCity = (EditText)findViewById(R.id.pCity);
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        pCity.setText(user.city);

        mSnackbar = Snackbar.make(ll, getString(R.string.small_login), Snackbar.LENGTH_LONG);
        snackbarView = mSnackbar.getView();
        snackTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);

        snackTextView.setTextColor(getResources().getColor(R.color.colorText));
        category = getIntent().getIntExtra("Category", 0);
        if(category != 0){
            pCategory.setText(categories[category - 1]);
            pCategory.setTextColor(getResources().getColor(R.color.colorBlackText));
            pCategory.setTextSize(18);
        }
        pCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AddActivity.this);
                builder.setTitle(getString(R.string.select_category));
                builder.setCancelable(false);
                builder.setItems(categories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pCategory.setText(categories[which]);
                        pCategory.setTextColor(getResources().getColor(R.color.colorBlackText));
                        pCategory.setTextSize(18);
                        category = which + 1;
                        dialog.cancel();
                    }
                });
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        button = (Button)findViewById(R.id.nextButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pName.length() < 6){
                    snackTextView.setText(getString(R.string.small_namePost));
                    mSnackbar.show();
                    return;
                }
                if(pText.length() < 32){
                    snackTextView.setText(getString(R.string.small_textPost));
                    mSnackbar.show();
                    return;
                }
                if(pCost.length() < 6){
                    snackTextView.setText(getString(R.string.small_costPost));
                    mSnackbar.show();
                    return;
                }
                if(category == 0){
                    snackTextView.setText(getString(R.string.category_no_selected));
                    pCategory.setTextSize(24);
                    pCategory.setTextColor(getResources().getColor(R.color.RED));
                    mSnackbar.show();
                    return;
                }
                CreatePost createPost = new CreatePost();
                createPost.execute();
            }
        });
        button.startAnimation(top);

        int mapAmount = getIntent().getIntExtra("qAmount", 0);

        if(mapAmount > 0){
            for(int i = 0; i != mapAmount; i++){
                String bom = "oldLatLng_" + i;
                coords.add(getIntent().getStringExtra(bom));
                bom = "oldAdress_" + i;
                adress.add(getIntent().getStringExtra(bom));
            }
            amount = mapAmount;
        }
        if(getIntent().getStringExtra("newAdress") != null){
            coords.add(getIntent().getStringExtra("newCoords"));
            adress.add(getIntent().getStringExtra("newAdress"));
            amount++;
        }
        if(getIntent().getStringExtra("qName") != null) pName.setText(getIntent().getStringExtra("qName"));
        if(getIntent().getStringExtra("qText") != null) pText.setText(getIntent().getStringExtra("qText"));
        if(getIntent().getStringExtra("qCost") != null) pCost.setText(getIntent().getStringExtra("qCost"));

        if(amount > 0){
            for(int i = 0; i != amount; i++) {
                final int number = i + 1;
                TextView adr = new TextView(this);
                adr.setText("Адрес №" + number + ": " + adress.get(i));
                adr.setTextColor(getResources().getColor(R.color.colorBlackText));
                adr.setTextSize(14);
                ll.addView(adr);
                adr.startAnimation(left);
            }
        }

        if(amount < 10) {
            TextView addAdres = new TextView(this);
            addAdres.setText(getString(R.string.add_Adress));
            addAdres.setTextSize(18);

            addAdres.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AddActivity.this, MapsActivity.class);
                    intent.putExtra("Name", pName.getText().toString());
                    intent.putExtra("Text", pText.getText().toString());
                    intent.putExtra("Cost", pCost.getText().toString());
                    intent.putExtra("Amount", amount);
                    intent.putExtra("Category", category);
                    intent.putExtra("User", user);
                    if (amount > 0) {
                        for (int i = 0; i != amount; i++) {
                            String name = "LatLng_" + i;
                            intent.putExtra(name, coords.get(i));
                            name = "Adress_" + i;
                            intent.putExtra(name, adress.get(i));
                        }
                    }
                    finish();
                    startActivity(intent);
                }
            });

            ll.addView(addAdres);
            addAdres.startAnimation(left);
        }
    }

    @Override
    public void onBackPressed() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AddActivity.this);
        builder.setTitle(getString(R.string.exit));
        builder.setMessage(getString(R.string.accept_exit_post));
        builder.setCancelable(false);
        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AddActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
            }
        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    class CreatePost extends AsyncTask<Void, Void, Integer>{

        TextView loadtext;
        int ownerID = user.id;
        String ownerLogin = user.login;
        String ownerName = user.name + " " + user.surname;
        String postName = pName.getText().toString();
        String postText = pText.getText().toString();
        String cost = pCost.getText().toString();
        String city = pCity.getText().toString();
        int status = 1;
        //int category = ca
        long createtime = System.currentTimeMillis() / 1000;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            button.startAnimation(top);
            ll.removeAllViews();
            loadtext = new TextView(getApplicationContext());
            loadtext.setText("Загрузка...");
            loadtext.setTextSize(24);
            loadtext.setGravity(Gravity.CENTER);
            loadtext.setTextColor(getResources().getColor(R.color.colorBlackText));
            ll.addView(loadtext);

        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(AddActivity.this, MainActivity.class);
            intent.putExtra("AddResult", aVoid);
            startActivity(intent);
        }

        @Override
        protected Integer doInBackground(Void... params) {

            String Adresses = "";
            for(int i = 0; i != adress.size(); i++) Adresses += adress.get(i) + "split";
            String Coordinates = "";
            for(int i = 0; i != coords.size(); i++) Coordinates += coords.get(i) + "split";
            //int amount;

            String[] keys = new String[] {"ownerID","ownerLogin","ownerName","postName","postText","cost","status","Category","createtime","Adresses","Coordinates","Amount","City"};
            Object[] objects = new Object[] {ownerID, ownerLogin, ownerName, postName, postText, cost, status, category, createtime, Adresses, Coordinates, amount, city};

            MySQL mysql = new MySQL("posts", false);
            mysql.insert(keys, objects);
            try {
                mysql.join(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mysql.close();
            mysql = null;

            user.score += 10;
            user.ad_amount++;
            user.update(new String[] {User.DB_SCORE, User.DB_AMOUNT_POSTS}, new Object[] {user.score, user.ad_amount});
            return 1;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AddActivity.this);
            builder.setTitle(getString(R.string.exit));
            builder.setMessage(getString(R.string.accept_exit_post));
            builder.setCancelable(false);
            builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(AddActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                }
            });
            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
