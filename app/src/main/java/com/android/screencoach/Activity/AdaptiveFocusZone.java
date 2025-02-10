package com.android.achievix.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import com.android.achievix.Database.LocationDAO;
import com.android.achievix.KalmanFilter;
import com.android.achievix.Model.LocationModel;
import com.android.achievix.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdaptiveFocusZone extends AppCompatActivity implements OnMapReadyCallback {

    private static final int FINE_PERMISSION_CODE = 1;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap map;
    private SearchView mapSearchView;
    private ImageButton mVoiceBtn;
    SwitchCompat satellite;
    private LatLng searchedLocation;
    private String locationName;
    private KalmanFilter latKalmanFilter;
    private KalmanFilter lonKalmanFilter;
    private LocationDAO locationDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adaptive_focus_zone);

        locationDAO = new LocationDAO(this);
        locationDAO.open();

        mapSearchView = findViewById(R.id.mapSearch);
        satellite = findViewById(R.id.satellite);
        mVoiceBtn = findViewById(R.id.voiceBtn);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;

                if (Geocoder.isPresent()) {
                    if (location != null && !location.isEmpty()) {
                        Geocoder geocoder = new Geocoder(AdaptiveFocusZone.this);
                        try {
                            addressList = geocoder.getFromLocationName(location, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (addressList != null && !addressList.isEmpty()) {
                            Address address = addressList.get(0);
                            searchedLocation = new LatLng(address.getLatitude(), address.getLongitude());
                            locationName = location; // This sets the name to the user input string
                            map.addMarker(new MarkerOptions().position(searchedLocation).title(location + " (" + searchedLocation.latitude + ", " + searchedLocation.longitude + ")"));
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 15));
                        } else {
                            Toast.makeText(AdaptiveFocusZone.this, "Location not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(AdaptiveFocusZone.this, "Geocoder service is not available on this device.", Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        mVoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        satellite.setOnCheckedChangeListener((buttonView, isChecked) -> onSwitchClick(isChecked));

        latKalmanFilter = new KalmanFilter(0, 1e-5, 1e-2, 1);
        lonKalmanFilter = new KalmanFilter(0, 1e-5, 1e-2, 1);

        findViewById(R.id.pinLoc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchedLocation != null) {
                    saveLocationDetails(locationName, searchedLocation.latitude, searchedLocation.longitude);
                    LocationModel location = new LocationModel(locationName, searchedLocation.latitude, searchedLocation.longitude);
                    locationDAO.addLocation(location);
                    Toast.makeText(AdaptiveFocusZone.this, "Location saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdaptiveFocusZone.this, SplashScreenActivityy.class);
                    intent.putExtra("locationName", locationName);
                    intent.putExtra("latitude", searchedLocation.latitude);
                    intent.putExtra("longitude", searchedLocation.longitude);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        locationDAO.close();
        super.onDestroy();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(AdaptiveFocusZone.this);

                    startLocationUpdates();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (currentLocation != null) {
            LatLng loc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.addMarker(new MarkerOptions().position(loc).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 20));
        }

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);

        loadSavedLocations();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (map != null) {
                    searchedLocation = latLng;
                    locationName = "Pinned Location (" + latLng.latitude + ", " + latLng.longitude + ")";
                    map.addMarker(new MarkerOptions().position(latLng).title(locationName));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        });
    }

    private void loadSavedLocations() {
        List<LocationModel> locations = locationDAO.getAllLocations();
        for (LocationModel location : locations) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.addMarker(new MarkerOptions().position(latLng).title(location.getName()));
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(6000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                List<Location> locations = locationResult.getLocations();
                double variance = calculateVariance(locations);

                double noiseThreshold = 0.0001;

                if (variance > noiseThreshold) {
                    for (Location location : locations) {
                        double kalmanLat = latKalmanFilter.update(location.getLatitude());
                        double kalmanLon = lonKalmanFilter.update(location.getLongitude());
                        LatLng kalmanLatLng = new LatLng(kalmanLat, kalmanLon);
                        updateMapLocation(kalmanLatLng);
                    }
                } else {
                    for (Location location : locations) {
                        LatLng rawLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        updateMapLocation(rawLatLng);
                    }
                }
            }
        }, null);
    }

    private double calculateVariance(List<Location> locations) {
        double meanLat = 0;
        double meanLon = 0;
        for (Location location : locations) {
            meanLat += location.getLatitude();
            meanLon += location.getLongitude();
        }
        meanLat /= locations.size();
        meanLon /= locations.size();

        double varianceLat = 0;
        double varianceLon = 0;
        for (Location location : locations) {
            varianceLat += Math.pow(location.getLatitude() - meanLat, 2);
            varianceLon += Math.pow(location.getLongitude() - meanLon, 2);
        }
        varianceLat /= locations.size();
        varianceLon /= locations.size();

        return (varianceLat + varianceLon) / 2;
    }

    private void updateMapLocation(LatLng latLng) {
        if (map != null) {
            map.clear();
            map.addMarker(new MarkerOptions().position(latLng).title("Filtered Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            loadSavedLocations(); // Ensure saved locations are still shown
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onSwitchClick(boolean isChecked) {
        if (map != null) {
            if (isChecked) {
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        } else {
            Toast.makeText(this, "Map is not yet ready", Toast.LENGTH_SHORT).show();
        }
    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi, speak something");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                mapSearchView.setQuery(spokenText, true); // This sets the spoken text in the SearchView and triggers the search
            }
        }
    }

    private void saveLocationDetails(String locationName, double latitude, double longitude) {
        SharedPreferences sharedPreferences = getSharedPreferences("LocationPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("LocationName", locationName);
        editor.putFloat("Latitude", (float) latitude);
        editor.putFloat("Longitude", (float) longitude);
        editor.apply();
    }
}
