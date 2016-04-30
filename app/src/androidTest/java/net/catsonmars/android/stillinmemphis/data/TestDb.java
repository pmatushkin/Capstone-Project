package net.catsonmars.android.stillinmemphis.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by pmatushkin on 4/27/2016.
 */
public class TestDb extends AndroidTestCase {

    void deleteDatabase() {
        mContext.deleteDatabase(TrackingDbHelper.DATABASE_NAME);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        deleteDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(TrackingContract.PackagesEntry.TABLE_NAME);
        tableNameHashSet.add(TrackingContract.EventsEntry.TABLE_NAME);

        deleteDatabase();
        SQLiteDatabase db = new TrackingDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while ( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the packages entry
        // and events entry tables
        assertTrue("Error: Your database was created without both the packages entry and events entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        // packages
        c = db.rawQuery("PRAGMA table_info(" + TrackingContract.PackagesEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        HashSet<String> columnHashSet = new HashSet<>();
        columnHashSet.add(TrackingContract.PackagesEntry._ID);
        columnHashSet.add(TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER);
        columnHashSet.add(TrackingContract.PackagesEntry.COLUMN_DESCRIPTION);
        columnHashSet.add(TrackingContract.PackagesEntry.COLUMN_ARCHIVED);
        columnHashSet.add(TrackingContract.PackagesEntry.COLUMN_DATE_ADDED);
        columnHashSet.add(TrackingContract.PackagesEntry.COLUMN_DATE_DELIVERED);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while ( c.moveToNext() );

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required packages entry columns",
                columnHashSet.isEmpty());

        // events
        c = db.rawQuery("PRAGMA table_info(" + TrackingContract.EventsEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        columnHashSet = new HashSet<>();
        columnHashSet.add(TrackingContract.EventsEntry._ID);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_PACKAGE_ID);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_EVENT_ORDER);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_TYPE);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_TIMESTAMP);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_TIME);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_DATE);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_EVENT);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_CITY);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_STATE);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_ZIP);
        columnHashSet.add(TrackingContract.EventsEntry.COLUMN_COUNTRY);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while ( c.moveToNext() );

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required events entry columns",
                columnHashSet.isEmpty());

        db.close();
    }

    public void testPackagesTable() {
        insertPackage();
    }

    public void testIncompletePackagesTable() {
        insertIncompletePackage();
    }

    public void testEventsTable() {
        // First insert the package, and then use the packageRowId to insert
        // the events.

        long packageRowId = insertPackage();

        // Make sure we have a valid row ID.
        assertFalse("Error: Package Not Inserted Correctly", packageRowId == -1L);

        // First step: Get reference to writable database
        TrackingDbHelper dbHelper = new TrackingDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Events): Create event values
        ContentValues eventValues = TestUtilities.createEventValues(packageRowId);

        // Third Step (Events): Insert ContentValues into database and get a row ID back
        long eventRowId = db.insert(TrackingContract.EventsEntry.TABLE_NAME, null, eventValues);
        assertTrue(eventRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        Cursor eventCursor = db.query(
                TrackingContract.EventsEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue( "Error: No records returned from events query", eventCursor.moveToFirst() );

        // Fifth Step: Validate the events query
        TestUtilities.validateCurrentRecord("Error: Events query validation failed",
                eventCursor, eventValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from events query",
                eventCursor.moveToNext() );

        // Sixth Step: Close cursor and database
        eventCursor.close();
        db.close();
        dbHelper.close();
    }

    public void testIncompleteEventsTable() {
        // First insert the package, and then use the packageRowId to insert
        // the events.

        long packageRowId = insertIncompletePackage();

        // Make sure we have a valid row ID.
        assertFalse("Error: Package Not Inserted Correctly", packageRowId == -1L);

        // First step: Get reference to writable database
        TrackingDbHelper dbHelper = new TrackingDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Events): Create event values
        ContentValues eventValues = TestUtilities.createIncompleteEventValues(packageRowId);

        // Third Step (Events): Insert ContentValues into database and get a row ID back
        long eventRowId = db.insert(TrackingContract.EventsEntry.TABLE_NAME, null, eventValues);
        assertTrue(eventRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        Cursor eventCursor = db.query(
                TrackingContract.EventsEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue( "Error: No records returned from events query", eventCursor.moveToFirst() );

        // Fifth Step: Validate the events query
        TestUtilities.validateCurrentRecord("Error: Events query validation failed",
                eventCursor, eventValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from events query",
                eventCursor.moveToNext() );

        // Sixth Step: Close cursor and database
        eventCursor.close();
        db.close();
        dbHelper.close();
    }

    public long insertPackage() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        TrackingDbHelper dbHelper = new TrackingDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createPackageValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long packageRowId;
        packageRowId = db.insert(TrackingContract.PackagesEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(packageRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                TrackingContract.PackagesEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No records returned from packages query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Packages query validation failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from packages query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        dbHelper.close();

        return packageRowId;
    }

    public long insertIncompletePackage() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        TrackingDbHelper dbHelper = new TrackingDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createIncompletePackageValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long packageRowId;
        packageRowId = db.insert(TrackingContract.PackagesEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(packageRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                TrackingContract.PackagesEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No records returned from packages query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Packages query validation failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from packages query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        dbHelper.close();

        return packageRowId;
    }
}
