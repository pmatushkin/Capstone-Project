package net.catsonmars.android.stillinmemphis.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.catsonmars.android.stillinmemphis.data.TrackingContract.EventsEntry;
import net.catsonmars.android.stillinmemphis.data.TrackingContract.PackagesEntry;

/**
 * Created by pmatushkin on 4/24/2016.
 */
public class TrackingDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "TrackingDbHelper";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "tracking.db";

    public TrackingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table to hold packages.
        final String SQL_CREATE_PACKAGES_TABLE = "CREATE TABLE " + PackagesEntry.TABLE_NAME + " (" +
                PackagesEntry._ID + " INTEGER PRIMARY KEY," +
                PackagesEntry.COLUMN_TRACKING_NUMBER + " TEXT UNIQUE NOT NULL, " +
                PackagesEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                PackagesEntry.COLUMN_ARCHIVED + " INTEGER NOT NULL, " +
                PackagesEntry.COLUMN_DATE_ADDED + " INTEGER NOT NULL, " +
                PackagesEntry.COLUMN_DATE_DELIVERED + " INTEGER NOT NULL " +
                " );";
        Log.d(TAG, SQL_CREATE_PACKAGES_TABLE);

        final String SQL_CREATE_EVENTS_TABLE = "CREATE TABLE " + EventsEntry.TABLE_NAME + " (" +
                EventsEntry._ID + " INTEGER PRIMARY KEY," +
                EventsEntry.COLUMN_PACKAGE_ID + " INTEGER NOT NULL, " +
                EventsEntry.COLUMN_EVENT_ORDER + " INTEGER NOT NULL, " +
                EventsEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                EventsEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                EventsEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                EventsEntry.COLUMN_DATE + " TEXT NOT NULL, " +

                EventsEntry.COLUMN_EVENT + " TEXT NOT NULL, " +
                EventsEntry.COLUMN_CITY + " TEXT NOT NULL, " +
                EventsEntry.COLUMN_STATE + " TEXT NOT NULL, " +
                EventsEntry.COLUMN_ZIP + " TEXT NOT NULL, " +
                EventsEntry.COLUMN_COUNTRY + " TEXT NOT NULL, " +

                // Set up the COLUMN_PACKAGE_ID column as a foreign key to Packages table.
                " FOREIGN KEY (" + EventsEntry.COLUMN_PACKAGE_ID + ") REFERENCES " +
                PackagesEntry.TABLE_NAME + " (" + PackagesEntry._ID + "));";
        Log.d(TAG, SQL_CREATE_EVENTS_TABLE);

        db.execSQL(SQL_CREATE_PACKAGES_TABLE);
        db.execSQL(SQL_CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + PackagesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EventsEntry.TABLE_NAME);

        onCreate(db);
    }
}
