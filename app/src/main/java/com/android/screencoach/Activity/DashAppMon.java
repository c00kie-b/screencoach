package com.android.achievix.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.achievix.R;

import java.util.Calendar;

public class DashAppMon extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private final Calendar c = Calendar.getInstance();
    private final int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
    private String greeting = "";

    @SuppressLint({"NonConstantResourceId", "InlinedApi", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_app_mon);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        TextView greet = findViewById(R.id.greet);
        greet.setText(getGreetings());
    }

    private void setupListeners() {
        // Setup other listeners here if needed in the future
        // Example: button.setOnClickListener(...);
    }

    public String getGreetings() {
        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = "Good Morning";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        return greeting;
    }

    public void onStatsButtonClick(View view) {
        startActivity(new Intent(DashAppMon.this, UsageOverviewActivity.class));
    }
}
