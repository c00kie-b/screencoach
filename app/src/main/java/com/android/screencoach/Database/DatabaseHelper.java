package com.android.achievix.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locations.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_LOCATIONS = "locations";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    public static final String TABLE_BLOCKED_APPS = "blocked_apps";
    public static final String COLUMN_APP_ID = "_id";
    public static final String COLUMN_LOCATION_ID = "location_id";
    public static final String COLUMN_APP_NAME = "app_name";

    private static final String TABLE_CREATE_LOCATIONS =
            "CREATE TABLE " + TABLE_LOCATIONS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL);";

    private static final String TABLE_CREATE_BLOCKED_APPS =
            "CREATE TABLE " + TABLE_BLOCKED_APPS + " (" +
                    COLUMN_APP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LOCATION_ID + " INTEGER, " +
                    COLUMN_APP_NAME + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_LOCATION_ID + ") REFERENCES " + TABLE_LOCATIONS + "(" + COLUMN_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_LOCATIONS);
        db.execSQL(TABLE_CREATE_BLOCKED_APPS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOCKED_APPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        onCreate(db);
    }
}
