package net.catsonmars.android.stillinmemphis.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by pmatushkin on 4/27/2016.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_PACKAGE = "9405803699300222655286";

    static ContentValues createPackageValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();

        Date currentDate = new Date();

        testValues.put(TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER, TEST_PACKAGE);
        testValues.put(TrackingContract.PackagesEntry.COLUMN_DESCRIPTION, "Documents");
        testValues.put(TrackingContract.PackagesEntry.COLUMN_ARCHIVED, 0);
        testValues.put(TrackingContract.PackagesEntry.COLUMN_DATE_ADDED, currentDate.getTime());
        testValues.put(TrackingContract.PackagesEntry.COLUMN_DATE_DELIVERED, currentDate.getTime());

        return testValues;
    }

    static ContentValues createIncompletePackageValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();

        Date currentDate = new Date();

        testValues.put(TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER, TEST_PACKAGE);
        testValues.put(TrackingContract.PackagesEntry.COLUMN_ARCHIVED, 0);
        testValues.put(TrackingContract.PackagesEntry.COLUMN_DATE_ADDED, currentDate.getTime());

        return testValues;
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();

            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);

            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    public static ContentValues createEventValues(long packageRowId) {
        ContentValues testValues = new ContentValues();

        Date currentDate = new Date();

        testValues.put(TrackingContract.EventsEntry.COLUMN_PACKAGE_ID, packageRowId);
        testValues.put(TrackingContract.EventsEntry.COLUMN_EVENT_ORDER, 0);
        testValues.put(TrackingContract.EventsEntry.COLUMN_TYPE, "event");
        testValues.put(TrackingContract.EventsEntry.COLUMN_TIMESTAMP, currentDate.getTime());
        testValues.put(TrackingContract.EventsEntry.COLUMN_TIME, "2:31 pm");
        testValues.put(TrackingContract.EventsEntry.COLUMN_DATE, "April 7, 2016");
        testValues.put(TrackingContract.EventsEntry.COLUMN_EVENT, "Delivered");
        testValues.put(TrackingContract.EventsEntry.COLUMN_CITY, "WOODSIDE");
        testValues.put(TrackingContract.EventsEntry.COLUMN_STATE, "NY");
        testValues.put(TrackingContract.EventsEntry.COLUMN_ZIP, "11377");
        testValues.put(TrackingContract.EventsEntry.COLUMN_COUNTRY, "");

        return testValues;
    }

    public static ContentValues createIncompleteEventValues(long packageRowId) {
        ContentValues testValues = new ContentValues();

        Date currentDate = new Date();

        testValues.put(TrackingContract.EventsEntry.COLUMN_PACKAGE_ID, packageRowId);
        testValues.put(TrackingContract.EventsEntry.COLUMN_EVENT_ORDER, 0);
        testValues.put(TrackingContract.EventsEntry.COLUMN_TYPE, "event");
        testValues.put(TrackingContract.EventsEntry.COLUMN_TIMESTAMP, currentDate.getTime());
        testValues.put(TrackingContract.EventsEntry.COLUMN_EVENT, "Delivered");

        return testValues;
    }
}
