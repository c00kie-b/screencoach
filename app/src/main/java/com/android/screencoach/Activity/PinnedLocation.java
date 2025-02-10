package com.android.achievix.Activity;

import java.util.List;

public class PinnedLocation {
    private String locationName;
    private float latitude;
    private float longitude;
    private List<String> blockedApps;

    public PinnedLocation(String locationName, float latitude, float longitude, List<String> blockedApps) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.blockedApps = blockedApps;
    }

    public String getLocationName() {
        return locationName;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public List<String> getBlockedApps() {
        return blockedApps;
    }
}
