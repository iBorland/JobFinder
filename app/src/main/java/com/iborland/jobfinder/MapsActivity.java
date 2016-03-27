package com.iborland.jobfinder;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Marker mark;
    GetAdress getAdress;
    ArrayList<String> coords = new ArrayList<String>();
    ArrayList<String> adresa = new ArrayList<String>();
    String name, text, cost;
    int amount;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        user = getIntent().getParcelableExtra("User");
        name = getIntent().getStringExtra("Name");
        text = getIntent().getStringExtra("Text");
        cost = getIntent().getStringExtra("Cost");
        amount = getIntent().getIntExtra("Amount", 0);
        if(amount > 0){
            for(int i = 0; i != amount; i++){
                String bom = "LatLng_" + i;
                coords.add(getIntent().getStringExtra(bom));
                bom = "Adress_" + i;
                adresa.add(getIntent().getStringExtra(bom));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng startPos = new LatLng(51.66177076370748,39.2021544277668);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 12));

        CamInMyPos();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(getAdress != null && getAdress.getStatus() == AsyncTask.Status.RUNNING){
                    Toast.makeText(MapsActivity.this, "В данный момент уже запущен поиск адреса", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mark == null) {
                    mark = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Адрес:")
                            .snippet("Загрузка данных")
                            .draggable(true));
                    mark.showInfoWindow();
                } else {
                    mark.remove();
                    mark = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Адрес:")
                            .snippet("Загрузка данных")
                            .draggable(true));
                    mark.showInfoWindow();
                }
                getAdress = new GetAdress();
                getAdress.execute(mark.getPosition());
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        UiSettings ui = mMap.getUiSettings();
        ui.setZoomGesturesEnabled(true);
        ui.setZoomControlsEnabled(true);
    }

    private void CamInMyPos() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        final LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1, 10, listener);

    }

    class GetAdress extends AsyncTask<LatLng, Void, String>{
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        String LOG_TAG = "ZAPROS";
        LatLng cords;

        @Override
        protected String doInBackground(LatLng... params) {
            // получаем данные с внешнего ресурса
            try {
                String adr = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + params[0].latitude + "" +
                        "," + params[0].longitude + "&key=AIzaSyA6iAbaZ1Ej0t701lFhU12LmWpJFoGfDmk&language=ru";
                URL url = new URL(adr);
                //Log.e("LOG", adr);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));
                cords = params[0];
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            JSONObject dataJsonObj = null;

            try {
                dataJsonObj = new JSONObject(strJson);
                JSONArray result = dataJsonObj.getJSONArray("results");
                JSONObject f_adress = result.getJSONObject(0);
                final String adress = f_adress.getString("formatted_address");

                mark.setSnippet(adress);
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Адрес");
                builder.setMessage(adress + "\n\nВы действительно хотите указать этот адрес, " +
                        "как адрес №" + (amount + 1) + " ?");
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
                        Intent intent = new Intent(MapsActivity.this, AddActivity.class);
                        intent.putExtra("qName", name);
                        intent.putExtra("qText", text);
                        intent.putExtra("qCost", cost);
                        intent.putExtra("qAmount", amount);
                        intent.putExtra("User", user);
                        if(amount > 0)
                        {
                            for(int i = 0; i != amount; i++){
                                String name = "oldLatLng_" + i;
                                intent.putExtra(name, coords.get(i));
                                name = "oldAdress_" + i;
                                intent.putExtra(name, adresa.get(i));
                            }
                        }
                        intent.putExtra("newAdress", adress);
                        intent.putExtra("newCoords", mark.getPosition().toString());
                        finish();
                        startActivity(intent);
                    }
                });
                android.support.v7.app.AlertDialog dialog = builder.create();
                dialog.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MapsActivity.this, AddActivity.class);
        intent.putExtra("qName", name);
        intent.putExtra("qText", text);
        intent.putExtra("qCost", cost);
        intent.putExtra("qAmount", amount);
        if(amount > 0)
        {
            for(int i = 0; i != amount; i++){
                String name = "oldLatLng_" + i;
                intent.putExtra(name, coords.get(i));
                name = "oldAdress_" + i;
                intent.putExtra(name, adresa.get(i));
            }
        }
        finish();
        startActivity(intent);
    }
}
