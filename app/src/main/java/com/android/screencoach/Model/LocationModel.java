package com.android.achievix.Model;

import java.util.List;

public class LocationModel {
    private long id;
    private String name;
    private double latitude;
    private double longitude;
    private List<String> blockedApps;

    // Default constructor
    public LocationModel() {
    }

    // Constructor with parameters
    public LocationModel(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters for all fields
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getBlockedApps() {
        return blockedApps;
    }

    public void setBlockedApps(List<String> blockedApps) {
        this.blockedApps = blockedApps;
    }


}
