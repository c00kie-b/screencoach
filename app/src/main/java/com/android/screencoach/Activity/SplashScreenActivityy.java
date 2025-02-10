package com.android.achievix.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.android.achievix.R;
import com.android.achievix.Permission.GetUsageStatsPermissionActivityy;

import java.util.Objects;

@SuppressLint({"CustomSplashScreen", "InlinedApi"})
public class SplashScreenActivityy extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash_screenn);

        SharedPreferences sh1 = getSharedPreferences("achievix", MODE_PRIVATE);
        SharedPreferences sh2 = getSharedPreferences("mode", MODE_PRIVATE);

        Handler handler = new Handler();

        if (Objects.equals(sh1.getString("firstTime", "yes"), "no")) {
            handler.postDelayed(() -> {
                int i = sh2.getInt("password", 0);
                Intent intent;

                if (i != 0) {
                    intent = new Intent(this, EnterPasswordActivityy.class);
                    intent.putExtra("password", i);
                    intent.putExtra("invokedFrom", "splashScreen");
                } else {
                    intent = new Intent(this, MainActivityy.class);
                }

                startActivity(intent);
                finish();
            }, 1800);
        } else {
            handler.postDelayed(() -> {
                Intent intent = new Intent(SplashScreenActivityy.this, GetUsageStatsPermissionActivityy.class);
                startActivity(intent);
                finish();
            }, 1800);
        }

        // Check for pinned location before proceeding to MainActivityy
        checkAndProceedToMain();
    }

    private void checkAndProceedToMain() {
        SharedPreferences sharedPreferences = getSharedPreferences("location", MODE_PRIVATE);
        String locationName = sharedPreferences.getString("locationName", "");

        // Check if location data is available
        if (!locationName.isEmpty()) {
            Intent intent = new Intent(this, MainActivityy.class);
            startActivity(intent);
            finish();
        }
    }
}
