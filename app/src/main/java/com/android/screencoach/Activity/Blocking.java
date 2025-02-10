package com.android.achievix.Activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.achievix.R;
import com.google.android.gms.maps.model.LatLng;

public class Blocking extends AppCompatActivity {


    private LatLng location;
    private TextView locationTextView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocking);


        // Retrieve the latitude and longitude values passed via the intent from the previous activity
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);
        String locationName = getIntent().getStringExtra("locationName");


        location = new LatLng(latitude, longitude);


        locationTextView = findViewById(R.id.location);
        locationTextView.setText("Location: " + locationName + "\nLatitude: " + latitude + "\nLongitude: " + longitude);
    }
}
