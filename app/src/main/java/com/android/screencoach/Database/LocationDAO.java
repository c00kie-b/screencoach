package com.android.achievix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.android.achievix.Model.LocationModel;

import java.util.ArrayList;
import java.util.List;

public class LocationDAO {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] allLocationColumns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_NAME,
            DatabaseHelper.COLUMN_LATITUDE,
            DatabaseHelper.COLUMN_LONGITUDE
    };
    private String[] allBlockedAppColumns = {
            DatabaseHelper.COLUMN_APP_ID,
            DatabaseHelper.COLUMN_LOCATION_ID,
            DatabaseHelper.COLUMN_APP_NAME
    };

    public LocationDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Add a single location without blocked apps
    public void addLocation(LocationModel location) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, location.getName());
        values.put(DatabaseHelper.COLUMN_LATITUDE, location.getLatitude());
        values.put(DatabaseHelper.COLUMN_LONGITUDE, location.getLongitude());

        long insertId = database.insert(DatabaseHelper.TABLE_LOCATIONS, null, values);
        location.setId(insertId);
    }

    // Add a location with blocked apps
    public void addLocationWithBlockedApps(LocationModel location, List<String> blockedApps) {
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NAME, location.getName());
            values.put(DatabaseHelper.COLUMN_LATITUDE, location.getLatitude());
            values.put(DatabaseHelper.COLUMN_LONGITUDE, location.getLongitude());

            long locationId = database.insert(DatabaseHelper.TABLE_LOCATIONS, null, values);
            location.setId(locationId);

            for (String app : blockedApps) {
                ContentValues appValues = new ContentValues();
                appValues.put(DatabaseHelper.COLUMN_LOCATION_ID, locationId);
                appValues.put(DatabaseHelper.COLUMN_APP_NAME, app);
                database.insert(DatabaseHelper.TABLE_BLOCKED_APPS, null, appValues);
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    // Get all locations without blocked apps
    public List<LocationModel> getAllLocations() {
        List<LocationModel> locations = new ArrayList<>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_LOCATIONS,
                allLocationColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LocationModel location = cursorToLocation(cursor);
            locations.add(location);
            cursor.moveToNext();
        }

        cursor.close();
        return locations;
    }

    public List<LocationModel> getLocationsWithBlockedApps() {
        List<LocationModel> locations = new ArrayList<>();

        String query = "SELECT DISTINCT " +
                "l." + DatabaseHelper.COLUMN_ID + ", " +
                "l." + DatabaseHelper.COLUMN_NAME + ", " +
                "l." + DatabaseHelper.COLUMN_LATITUDE + ", " +
                "l." + DatabaseHelper.COLUMN_LONGITUDE +
                " FROM " + DatabaseHelper.TABLE_LOCATIONS + " l" +
                " INNER JOIN " + DatabaseHelper.TABLE_BLOCKED_APPS + " b" +
                " ON l." + DatabaseHelper.COLUMN_ID + " = b." + DatabaseHelper.COLUMN_LOCATION_ID;

        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LocationModel location = cursorToLocation(cursor);
            locations.add(location);
            cursor.moveToNext();
        }
        cursor.close();
        return locations;
    }

    private LocationModel cursorToLocation(Cursor cursor) {
        LocationModel location = new LocationModel();
        location.setId(cursor.getLong(0));
        location.setName(cursor.getString(1));
        location.setLatitude(cursor.getDouble(2));
        location.setLongitude(cursor.getDouble(3));
        return location;
    }

    private List<String> getBlockedAppsForLocation(long locationId) {
        List<String> blockedApps = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_BLOCKED_APPS,
                allBlockedAppColumns,
                DatabaseHelper.COLUMN_LOCATION_ID + " = ?",
                new String[]{String.valueOf(locationId)},
                null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            blockedApps.add(cursor.getString(2));
            cursor.moveToNext();
        }
        cursor.close();
        return blockedApps;
    }

    // Add a new method to delete a location by its name
    public void deleteLocation(String locationName) {
        String whereClause = DatabaseHelper.COLUMN_NAME + " = ?";
        String[] whereArgs = new String[]{locationName};

        // Delete blocked apps associated with this location
        Cursor cursor = database.query(DatabaseHelper.TABLE_LOCATIONS, allLocationColumns, whereClause, whereArgs, null, null, null);
        if (cursor.moveToFirst()) {
            long locationId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            database.delete(DatabaseHelper.TABLE_BLOCKED_APPS, DatabaseHelper.COLUMN_LOCATION_ID + " = ?", new String[]{String.valueOf(locationId)});
        }
        cursor.close();

        // Delete the location
        database.delete(DatabaseHelper.TABLE_LOCATIONS, whereClause, whereArgs);
    }

    // Delete all locations and their associated blocked apps
    public void deleteAllLocations() {
        database.delete(DatabaseHelper.TABLE_BLOCKED_APPS, null, null);
        database.delete(DatabaseHelper.TABLE_LOCATIONS, null, null);
    }
}
