package com.android.achievix.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.achievix.Database.LocationDAO;
import com.android.achievix.Model.LocationModel;
import com.android.achievix.R;
import com.android.achievix.Model.AppBlockModell;
import com.android.achievix.Service.MyAccessibilityService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LocationTriggerActivityy extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<AppBlockModell> storedApps;
    private TextView tvStoredApps;
    private TextView tvStoredLocation;
    private FusedLocationProviderClient fusedLocationClient;

    private Handler handler;
    private Runnable idCheckRunnable;
    private Runnable jsonDisplayRunnable;

    private LocationDAO locationDAO;
    private boolean appsLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_trigger_activityy);

        locationDAO = new LocationDAO(this);
        locationDAO.open();

        storedApps = getIntent().getParcelableArrayListExtra("selectedApps");
        tvStoredApps = findViewById(R.id.tvStoredApps);
        tvStoredLocation = findViewById(R.id.tvStoredLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        handler = new Handler();

        try {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } catch (Exception e) {
            Log.e("LocationTriggerActivityy", "Error applying window insets", e);
        }

        Button btnTriggerLocation = findViewById(R.id.btnTriggerLocation);
        btnTriggerLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAccessibilityServiceEnabled()) {
                    Toast.makeText(LocationTriggerActivityy.this, "Please enable accessibility service", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                } else {
                    displayStoredApps();
                    requestLocationPermission();
                    startDisplayingJson(); // Start displaying the JSON string
                }
            }
        });

        Button backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> onBackPressed()); // Handle back button click
    }

    @Override
    public void onBackPressed() {
        // Start HomeActivity when back button is pressed
        super.onBackPressed();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finish current activity
    }

    private boolean isAccessibilityServiceEnabled() {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("LocationTriggerActivityy", "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                String[] enabledServices = settingValue.split(":");
                for (String enabledService : enabledServices) {
                    if (enabledService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void startDisplayingJson() {
        jsonDisplayRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("LocationTriggerActivityy", "Checking location discrepancy");
                SharedPreferences sharedPreferences = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE);
                Map<String, ?> allEntries = sharedPreferences.getAll();
                Gson gson = new Gson();

                // Check location permissions
                if (ActivityCompat.checkSelfPermission(LocationTriggerActivityy.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(LocationTriggerActivityy.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Request permissions if not granted
                    requestLocationPermission();
                    return;
                }

                fusedLocationClient.getLastLocation().addOnSuccessListener(LocationTriggerActivityy.this, location -> {
                    if (location != null) {
                        boolean anyMatch = false; // This flag indicates if any location is within 500 meters

                        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                            Object value = entry.getValue();
                            if (value instanceof String && isUUID(entry.getKey())) {
                                String uniqueID = entry.getKey();
                                String pinnedLocationJson = (String) value;
                                Log.d("LocationTriggerActivityy", "Fetching JSON for key: " + uniqueID + ", JSON: " + pinnedLocationJson);
                                try {
                                    PinnedLocation pinnedLocation = gson.fromJson(pinnedLocationJson, PinnedLocation.class);
                                    String message = "Location: " + pinnedLocation.getLocationName() + ", Lat: " + pinnedLocation.getLatitude() +
                                            ", Long: " + pinnedLocation.getLongitude() + ", Apps: " + pinnedLocation.getBlockedApps();
                                    Log.d("LocationTriggerActivityy", message);

                                    // Calculate distance to saved location
                                    Location savedLocation = new Location("");
                                    savedLocation.setLatitude(pinnedLocation.getLatitude());
                                    savedLocation.setLongitude(pinnedLocation.getLongitude());

                                    float distance = location.distanceTo(savedLocation);
                                    Log.d("LocationTriggerActivityy", "Distance to saved location: " + distance + " meters");

                                    // Check distance and lock/unlock applications
                                    if (distance <= 500) {
                                        lockApplicationsForLocation(pinnedLocation);
                                        anyMatch = true; // Set flag to true if any location matches
                                    } else {
                                        unlockApplicationsForLocation(pinnedLocation);
                                    }
                                } catch (JsonSyntaxException e) {
                                    Log.e("LocationTriggerActivityy", "Failed to parse JSON for key: " + uniqueID + ", JSON: " + pinnedLocationJson, e);
                                }
                            }
                        }

                        // Unlock applications only if no location matches
                        if (!anyMatch && appsLocked) {
                            Toast.makeText(LocationTriggerActivityy.this, "Unlocking all applications due to no location match.", Toast.LENGTH_LONG).show();
                            Log.d("LocationTriggerActivityy", "Unlocking applications.");
                            unlockAllApplications();
                        } else {
                            Log.d("LocationTriggerActivityy", "No changes in application lock status.");
                        }
                    } else {
                        Log.e("LocationTriggerActivityy", "Failed to get current location.");
                    }
                }).addOnFailureListener(LocationTriggerActivityy.this, e -> {
                    Log.e("LocationTriggerActivityy", "Error getting last location", e);
                });

                handler.postDelayed(this, 5000); // Repeat every 5 seconds
            }
        };
        handler.postDelayed(jsonDisplayRunnable, 5000); // Start after 5 seconds
    }

    private Set<AppBlockModell> currentlyLockedApps = new HashSet<>();

    private void lockApplicationsForLocation(PinnedLocation pinnedLocation) {
        List<AppBlockModell> appsToLock = new ArrayList<>();
        for (String appName : pinnedLocation.getBlockedApps()) {
            for (AppBlockModell app : storedApps) {
                if (app.getAppName().equals(appName) && !currentlyLockedApps.contains(app)) {
                    appsToLock.add(app);
                }
            }
        }
        if (!appsToLock.isEmpty()) {
            currentlyLockedApps.addAll(appsToLock);
            MyAccessibilityService.setLockedApps(new ArrayList<>(currentlyLockedApps));
            Log.d("LocationTriggerActivityy", "Applications locked for location: " + pinnedLocation.getLocationName());
        }
    }

    private void unlockApplicationsForLocation(PinnedLocation pinnedLocation) {
        List<AppBlockModell> appsToUnlock = new ArrayList<>();
        for (String appName : pinnedLocation.getBlockedApps()) {
            for (AppBlockModell app : storedApps) {
                if (app.getAppName().equals(appName) && currentlyLockedApps.contains(app)) {
                    appsToUnlock.add(app);
                }
            }
        }
        if (!appsToUnlock.isEmpty()) {
            currentlyLockedApps.removeAll(appsToUnlock);
            MyAccessibilityService.setLockedApps(new ArrayList<>(currentlyLockedApps));
            Log.d("LocationTriggerActivityy", "Applications unlocked for location: " + pinnedLocation.getLocationName());
        }
    }

    private void unlockAllApplications() {
        currentlyLockedApps.clear();
        MyAccessibilityService.setLockedApps(new ArrayList<>());
        appsLocked = false;
        Log.d("LocationTriggerActivityy", "All applications unlocked.");
    }

    private boolean isUUID(String key) {
        try {
            UUID.fromString(key);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void displayStoredApps() {
        if (storedApps != null && !storedApps.isEmpty()) {
            StringBuilder appsList = new StringBuilder("Locked Applications:\n");
            for (AppBlockModell app : storedApps) {
                appsList.append(app.getAppName()).append("\n");
            }
            tvStoredApps.setText(appsList.toString());
        } else {
            tvStoredApps.setText("Locked Applications:\nNo stored applications found");
        }
    }

    private void lockApplications() {
        if (storedApps != null && !storedApps.isEmpty()) {
            MyAccessibilityService.setLockedApps(storedApps);
            appsLocked = true;
            Log.d("LocationTriggerActivityy", "Applications locked.");
        }
    }

    private void unlockApplications() {
        MyAccessibilityService.setLockedApps(new ArrayList<>()); // Use an empty list instead of null
        appsLocked = false;
        Log.d("LocationTriggerActivityy", "Applications unlocked.");
    }

    private void saveLocationAndApps() {
        SharedPreferences sharedPreferences = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String locationName = sharedPreferences.getString("LocationName", "No location saved");
        float latitude = sharedPreferences.getFloat("Latitude", 0);
        float longitude = sharedPreferences.getFloat("Longitude", 0);

        List<String> blockedApps = new ArrayList<>();
        for (AppBlockModell app : storedApps) {
            blockedApps.add(app.getAppName());
        }

        if (!blockedApps.isEmpty()) {
            PinnedLocation pinnedLocation = new PinnedLocation(locationName, latitude, longitude, blockedApps);
            Gson gson = new Gson();
            String pinnedLocationJson = gson.toJson(pinnedLocation);

            String uniqueID = UUID.randomUUID().toString();
            editor.putString(uniqueID, pinnedLocationJson);
            editor.apply();

            String displayText = "Stored Location: \n" + locationName + "\nLatitude: " + latitude + "\nLongitude: " + longitude;
            tvStoredLocation.setText(displayText);

            // Log the details in Logcat
            Log.d("LocationTriggerActivity", "Saved Location: " + locationName + " (Lat: " + latitude + ", Long: " + longitude + ")");
            Log.d("LocationTriggerActivity", "Blocked Apps: " + blockedApps);
            Log.d("LocationTriggerActivity", "New unique ID generated and saved: " + uniqueID);
            Log.d("LocationTriggerActivity", "Saved JSON: " + pinnedLocationJson);
        } else {
            Log.d("LocationTriggerActivity", "No blocked apps to save.");
        }

        // Add the location and blocked apps to the database
        LocationModel locationModel = new LocationModel();
        locationModel.setName(locationName);
        locationModel.setLatitude(latitude);
        locationModel.setLongitude(longitude);
        locationDAO.addLocationWithBlockedApps(locationModel, blockedApps);

        locationDAO.close();
    }

    private void logPinnedLocationWithUniqueID() {
        SharedPreferences sharedPreferences = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE);
        String locationName = sharedPreferences.getString("LocationName", "No location saved");
        float latitude = sharedPreferences.getFloat("Latitude", 0);
        float longitude = sharedPreferences.getFloat("Longitude", 0);
        String uniqueID = sharedPreferences.getString("LockStateID", "No ID");

        Log.d("LocationTriggerActivityy", "Pinned Location: " + locationName + " (Lat: " + latitude + ", Long: " + longitude + ") with ID: " + uniqueID);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocationAndCheckDiscrepancy();
        }
    }

    private void getCurrentLocationAndCheckDiscrepancy() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Log.d("LocationTriggerActivityy", "Current location: Latitude = " + location.getLatitude() + ", Longitude = " + location.getLongitude());
                        checkLocationDiscrepancy(location);
                    } else {
                        Log.e("LocationTriggerActivityy", "Failed to get current location.");
                    }
                });
            } catch (SecurityException e) {
                Log.e("LocationTriggerActivityy", "Location permission error", e);
            }
        }
    }

    private void checkLocationDiscrepancy(Location currentLocation) {
        SharedPreferences sharedPreferences = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE);
        String locationName = sharedPreferences.getString("LocationName", "No location saved");
        float savedLatitude = sharedPreferences.getFloat("Latitude", 0);
        float savedLongitude = sharedPreferences.getFloat("Longitude", 0);

        Location savedLocation = new Location("");
        savedLocation.setLatitude(savedLatitude);
        savedLocation.setLongitude(savedLongitude);

        float distance = currentLocation.distanceTo(savedLocation);
        Log.d("LocationTriggerActivityy", "Distance to saved location: " + distance + " meters");

        if (distance <= 60) {
            if (!appsLocked) {
                Toast.makeText(this, "Locking all applications due to location match.", Toast.LENGTH_LONG).show();
                Log.d("LocationTriggerActivityy", "Locking applications.");
                lockApplications();
            }
        } else {
            if (appsLocked) {
                Toast.makeText(this, "Unlocking all applications due to no location match.", Toast.LENGTH_LONG).show();
                Log.d("LocationTriggerActivityy", "Unlocking applications.");
                unlockApplications();
            }
        }

        saveLocationAndApps();
        logPinnedLocationWithUniqueID(); // Log the pinned location with unique ID
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndCheckDiscrepancy();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
