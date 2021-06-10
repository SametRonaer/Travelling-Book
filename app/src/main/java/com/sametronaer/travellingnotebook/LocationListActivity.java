package com.sametronaer.travellingnotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.sametronaer.travellingnotebook.models.LocationModel;

import java.util.ArrayList;

public class LocationListActivity extends AppCompatActivity {

    Intent mapIntent;
    Intent seeLocationIntent;
    SQLiteDatabase sqLiteDatabase;
    ListView listView;
    ArrayList<String> locationList;
    ArrayAdapter arrayAdapter;
    Intent dataIntent;
    LocationModel locationModel;
    LocationManager locationManager;
    LocationListener locationListener;
    double currentLat, currentLongt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        mapIntent = new Intent(LocationListActivity.this, MapsActivity.class);
        listView = findViewById(R.id.listView);
        locationManager =  (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                System.out.println("Current loc"+ location.getLatitude());;
            }
        };

        locationList = new ArrayList<String>();
        dataIntent = getIntent();
        seeLocationIntent = new Intent(LocationListActivity.this, MapsActivity.class);
        createDatabase();
        setListView();
        setlistViewActions();



        try {
            locationModel = (LocationModel) dataIntent.getSerializableExtra("locationObject");
            locationList.add(locationModel.getCountryName());
            saveLocation(locationModel);


        }catch (Exception e){
            e.printStackTrace();
        }

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, locationList);
        listView.setAdapter(arrayAdapter);

       // createDatabase();

    }

    private void createDatabase() {
        try {
            sqLiteDatabase = this.openOrCreateDatabase("Locations", MODE_PRIVATE, null);
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS locations (id INTEGER PRIMARY KEY, country VARCHAR, city VARCHAR, street VARCHAR, latitude DOUBLE, longtitude DOUBLE)");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addNewPlace(View view){
        System.out.println("Floatind Action Button Pressed");
        getPermission();
        startActivity(mapIntent);
    }

    private void saveLocation(LocationModel saveLocation){
        String countryName = saveLocation.getCountryName();
        String cityName = "..";
        String streetName = saveLocation.getStreetName();
        double locationLongt = saveLocation.getLongt();
        double locationLat = saveLocation.getLat();
       try{
           String sqlQuery =  "INSERT INTO locations (country, city, street, latitude, longtitude) VALUES(?,?,?,?,?)";
           SQLiteStatement sqLiteStatement = sqLiteDatabase.compileStatement(sqlQuery);
           sqLiteStatement.bindString(1, countryName);
           sqLiteStatement.bindString(2, cityName);
           if (streetName != null){
               sqLiteStatement.bindString(3, streetName);
           }else {
               sqLiteStatement.bindString(3,"noStreet");
           }
           sqLiteStatement.bindDouble(4, locationLat);
           sqLiteStatement.bindDouble(5, locationLongt);
           sqLiteStatement.execute();
       }catch (Exception e){
           e.printStackTrace();
           System.out.println("Kaydedilemedi: "+ e.getCause()+".."+e.getMessage());
       }

    }

    private void setListView(){
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM locations", null);
        int countryIndex = cursor.getColumnIndex("country");
        int cityIndex = cursor.getColumnIndex("city");
        int streetIndex = cursor.getColumnIndex("street");
        locationList.clear();
        while(cursor.moveToNext()){
            locationList.add(cursor.getString(countryIndex));
        }
    }

    private void setlistViewActions(){

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(locationList.get(position));
                String countryName = locationList.get(position);
                String query = "SELECT * FROM locations WHERE country = '"+ countryName+"'";
                Cursor cursor = sqLiteDatabase.rawQuery(query, null);
                int latIndex = cursor.getColumnIndex("latitude");
                int longtIndex = cursor.getColumnIndex("longtitude");
                double selectedLat, selectedLong;
                while (cursor.moveToNext()){
                    System.out.println("Database lat");
                    selectedLat = cursor.getDouble(latIndex);
                    selectedLong = cursor.getDouble(longtIndex);
                    seeLocationIntent.putExtra("lat", selectedLat);
                    seeLocationIntent.putExtra("longt", selectedLong);
                    seeLocationIntent.putExtra("info","info");
                    seeLocationIntent.putExtra("country",countryName);

                }
                startActivity(seeLocationIntent);

            }
        });
    }

    private void getPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            currentLat = location.getLatitude();
            currentLongt = location.getLongitude();
            mapIntent.putExtra("lat", currentLat);
            mapIntent.putExtra("longt", currentLongt);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 10, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                currentLat = location.getLatitude();
                currentLongt = location.getLongitude();
                mapIntent.putExtra("lat", currentLat);
                mapIntent.putExtra("longt", currentLongt);
            }
        }
    }
}