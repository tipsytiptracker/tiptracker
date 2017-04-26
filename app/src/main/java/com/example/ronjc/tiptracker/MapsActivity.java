package com.example.ronjc.tiptracker;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<LatLng> locations;
    private ArrayList<String> names;
    private double currLat, currLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        locations = intent.getParcelableArrayListExtra("locations");
        names = intent.getStringArrayListExtra("names");
        currLat = intent.getDoubleExtra("latitude", 0);
        currLong = intent.getDoubleExtra("longitude", 0);
//        Bundle bundle = intent.getBundleExtra("bundle");
//        locations = bundle.getBundle().get("locations");
//        names = (ArrayList<String>) bundle.get("names");
//        currLat = bundle
//        currLong = intent.getDoubleExtra("longitude", 0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if(locations.size() == 0) {
            LatLng currentLocation = new LatLng(currLat, currLong);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        } else {
            int i = 0;
            for(LatLng location : locations) {
                mMap.addMarker(new MarkerOptions().position(location).title(names.get(i)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f));
                i++;
            }
        }
    }
}
