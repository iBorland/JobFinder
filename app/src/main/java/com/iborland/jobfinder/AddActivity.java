package com.iborland.jobfinder;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
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

    EditText pName, pText, pCost;
    TextView pCategory;
    LinearLayout ll;
    Button button;
    String[] categories;
    int amount = 0, category = 0;
    ArrayList<String> coords = new ArrayList<String>();
    ArrayList<String> adress = new ArrayList<String>();
    Animation top, left, return_left;
    User user;

    Connection connection = null;
    Statement statement = null;

    Snackbar mSnackbar;
    View snackbarView;
    TextView snackTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        ll = (LinearLayout)findViewById(R.id.LinearInScroll);
        user = getIntent().getParcelableExtra("User");
        top = AnimationUtils.loadAnimation(this, android.support.design.R.anim.abc_slide_in_top);
        left = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        return_left = AnimationUtils.loadAnimation(this, R.anim.return_slide_left);
        categories = getResources().getStringArray(R.array.categories);
        pName = (EditText)findViewById(R.id.pName);
        pText = (EditText)findViewById(R.id.pText);
        pCost = (EditText)findViewById(R.id.pCost);
        pCategory = (TextView)findViewById(R.id.pCategory);

        mSnackbar = Snackbar.make(ll, "Слишком короткий логин", Snackbar.LENGTH_LONG);
        snackbarView = mSnackbar.getView();
        snackTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);

        snackTextView.setTextColor(getResources().getColor(R.color.colorText));
        pCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AddActivity.this);
                builder.setTitle("Выберите категорию:");
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
                    snackTextView.setText("Слишком короткое название объявления");
                    mSnackbar.show();
                    return;
                }
                if(pText.length() < 32){
                    snackTextView.setText("Текст объявления должен содержать минимум 32 символа");
                    mSnackbar.show();
                    return;
                }
                if(pCost.length() < 6){
                    snackTextView.setText("Слишком короткое описание оплаты");
                    mSnackbar.show();
                    return;
                }
                if(category == 0){
                    snackTextView.setText("Вы не выбрали категорию");
                    pCategory.setTextSize(24);
                    pCategory.setTextColor(getResources().getColor(R.color.RED));
                    mSnackbar.show();
                    return;
                }
                CreatePost createPost = new CreatePost();
                createPost.execute();
            }
        });

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
            addAdres.setText("+ добавить адрес");
            addAdres.setTextSize(18);

            addAdres.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AddActivity.this, MapsActivity.class);
                    intent.putExtra("Name", pName.getText().toString());
                    intent.putExtra("Text", pText.getText().toString());
                    intent.putExtra("Cost", pCost.getText().toString());
                    intent.putExtra("Amount", amount);
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
        builder.setTitle("Выход");
        builder.setMessage("Вы действительно хотите завершить создание объявления?\n\n" +
                "Внимение: Если вы покините создание нового объявления, все введённые вами данные будут потеряны.");
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
        String ownerName = user.login;
        String postName = pName.getText().toString();
        String postText = pText.getText().toString();
        String cost = pCost.getText().toString();
        int status = 1;
        //int category = ca
        long createtime = System.currentTimeMillis() / 1000;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll.removeAllViews();
            loadtext = new TextView(getApplicationContext());
            loadtext.setText("Загрузка...");
            loadtext.setTextSize(24);
            loadtext.setGravity(Gravity.CENTER);
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

            String query = "INSERT INTO `posts` (`ownerID`, `ownerName`,`postName`,`postText`," +
                    "`cost`,`status`,`Category`,`createtime`,`Adresses`,`Coordinates`,`Amount`)" +
                    " VALUES ('" + ownerID + "', '" + ownerName + "','" + postName + "'," +
                    "'" + postText + "','" + cost + "','" + status + "','" + category + "'" +
                    ",'" + createtime + "','" + Adresses + "','" + Coordinates + "','" + amount + "')";
            String rightquery = "";
            try{
                rightquery = new String(query.getBytes("utf-8"), "utf-8");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            Log.e("QUERY", rightquery);

            try{
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + MainActivity.db_ip, MainActivity.db_login,
                        MainActivity.db_password);
                statement = connection.createStatement();
                statement.executeUpdate(rightquery);
                statement.close();
                connection.close();
            }
            catch (Exception e){
                e.printStackTrace();
                return 0;
            }
            return 1;
        }


    }
}
