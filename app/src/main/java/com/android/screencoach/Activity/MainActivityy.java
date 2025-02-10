package com.android.achievix.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.achievix.R;
import com.android.achievix.Database.BlockDatabasee;
import com.android.achievix.Model.ProfileModell;
import com.android.achievix.Service.ForegroundServicee;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivityy extends AppCompatActivity {
    private LinearLayout ll1;
    private final Calendar c = Calendar.getInstance();
    private final int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
    private String greeting = "";

    private TextView appBlockCount;
    private final BlockDatabasee blockDatabase = new BlockDatabasee(this);

    @SuppressLint({"NonConstantResourceId", "InlinedApi", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainn);

        // Check and request permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        // Check if foreground service is running and start if not
        if (!isMyServiceRunning(ForegroundServicee.class)) {
            Intent serviceIntent = new Intent(this, ForegroundServicee.class);
            serviceIntent.putExtra("inputExtra", "Foreground Service is Running");
            ContextCompat.startForegroundService(this, serviceIntent);
        }

        // Initialize views and setup listeners
        initializeViews();
        initializeCount();
        setupListeners();
        initRecyclerView();

        // Check for pinned location and start blocking if available
        checkAndStartBlocking();
    }

    private void initializeViews() {
        ll1 = findViewById(R.id.goto_block_apps);
        appBlockCount = findViewById(R.id.main_app_blocks);
    }

    private void initializeCount() {
        appBlockCount.setText(String.valueOf(blockDatabase.getAppBlockCount()));
    }

    private void setupListeners() {
        NavigationView navigationMenu = findViewById(R.id.nav_menu);
        navigationMenu.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu2:
                    startActivity(new Intent(MainActivityy.this, AboutActivityy.class));
                    break;
            }
            return false;
        });

        ll1.setOnClickListener(view12 -> startActivity(new Intent(this, AppBlockActivityy.class)));
    }

    private void initRecyclerView() {
        // Initialize RecyclerView
        // This part seems unrelated to pinning or blocking logic, so I omitted it for brevity
    }

    private void showModeDialog() {
        // Show mode dialog
        // This part seems unrelated to pinning or blocking logic, so I omitted it for brevity
    }

    @SuppressLint("SetTextI18n")
    private void updateMode(boolean _strict) {
        // Update mode logic
        // This part seems unrelated to pinning or blocking logic, so I omitted it for brevity
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        // Check if service is running
        // This part seems unrelated to pinning or blocking logic, so I omitted it for brevity
        return false;
    }


    public String getGreetings() {
        // Get greeting based on time of day
        // This part seems unrelated to pinning or blocking logic, so I omitted it for brevity
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                SharedPreferences sh = getSharedPreferences("mode", MODE_PRIVATE);
                boolean _strict = sh.getBoolean("strict", false);
                updateMode(_strict);
            }
        }
    }

    private void checkAndStartBlocking() {
        SharedPreferences sharedPreferences = getSharedPreferences("location", MODE_PRIVATE);
        String locationName = sharedPreferences.getString("locationName", "");
        double latitude = sharedPreferences.getFloat("latitude", 0);
        double longitude = sharedPreferences.getFloat("longitude", 0);

        // Check if location data is available
        if (!locationName.isEmpty() && latitude != 0 && longitude != 0) {
            // Start blocking logic here
            // You can start blocking services or perform other actions based on the pinned location
            // For demonstration purposes, I'm just showing a Toast
            Toast.makeText(this, "Start blocking for location: " + locationName, Toast.LENGTH_SHORT).show();
        }
    }
}
