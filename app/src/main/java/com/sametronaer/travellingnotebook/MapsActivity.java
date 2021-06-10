package com.sametronaer.travellingnotebook;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sametronaer.travellingnotebook.models.LocationModel;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationModel locationModel;
    Geocoder geocoder;
    List<Address> locationDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       // locationDetails = new ArrayList<String>();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        //mMap.setOnMapClickListener(this);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        detectIntent();
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("Save Location?").setMessage("Are you sure to save this place?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveLocation(latLng);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();


    }

    private void saveLocation(LatLng latLng){
        locationModel = new LocationModel();
        geocoder = new  Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            locationDetails = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        locationModel.setCountryName(locationDetails.get(0).getCountryName());
        locationModel.setStreetName(locationDetails.get(0).getThoroughfare());
        locationModel.setLat(locationDetails.get(0).getLatitude());
        locationModel.setLongt(locationDetails.get(0).getLongitude());

        Intent locationListIntent = new Intent(MapsActivity.this, LocationListActivity.class);
        locationListIntent.putExtra("locationObject", locationModel);
        startActivity(locationListIntent);
    }

    private void setCurrentLocation(LatLng currentLatLng){
        mMap.addMarker(new MarkerOptions().position(currentLatLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 5));
    }

    private void detectIntent(){
        if(getIntent().getStringExtra("info") != null){
            double lat = getIntent().getDoubleExtra("lat", 0);
            double longt = getIntent().getDoubleExtra("longt", 0);
            String country = getIntent().getStringExtra("country");
            LatLng latLng = new LatLng(lat, longt);
            mMap.addMarker(new MarkerOptions().position(latLng).title(country));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        }else{
            double lat = getIntent().getDoubleExtra("lat", 0);
            double longt = getIntent().getDoubleExtra("longt", 0);
            LatLng currentLatLng = new LatLng(lat,longt);
            setCurrentLocation(currentLatLng);
        }
    }

}