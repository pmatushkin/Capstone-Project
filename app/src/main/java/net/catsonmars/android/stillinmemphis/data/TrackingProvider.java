package net.catsonmars.android.stillinmemphis.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import net.catsonmars.android.stillinmemphis.sync.StillInMemphisSyncAdapter;

/**
 * Created by pmatushkin on 4/24/2016.
 */
public class TrackingProvider extends ContentProvider {
    private static final String TAG = "TrackingProvider";
    private static final String TAG_PRINT_DATABASE = "PRINT_DATABASE";

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TrackingDbHelper mOpenHelper;

    static final int PACKAGES = 100;
    static final int PACKAGE_WITH_EVENTS = 101;
    static final int PACKAGES_WITH_LATEST_EVENT = 102;
    static final int EVENTS = 200;

    private static final SQLiteQueryBuilder sEventsByPackageQueryBuilder;

    static {
        Log.d(TAG, "TrackingProvider.static");
        Log.d(TAG, TrackingContract.joinString);

        sEventsByPackageQueryBuilder = new SQLiteQueryBuilder();
        sEventsByPackageQueryBuilder.setTables(TrackingContract.joinString);
    }

    static UriMatcher buildUriMatcher() {
        Log.d(TAG, "TrackingProvider.buildUriMatcher()");

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TrackingContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, TrackingContract.PATH_PACKAGES, PACKAGES);
        matcher.addURI(authority, TrackingContract.PATH_PACKAGES + "/#/*", PACKAGE_WITH_EVENTS);
        matcher.addURI(authority, TrackingContract.PATH_PACKAGES + "/*/0", PACKAGES_WITH_LATEST_EVENT);

        matcher.addURI(authority, TrackingContract.PATH_EVENTS, EVENTS);

        return matcher;
    }

    private Cursor getEventsForPackage(Uri uri,
                                       String[] projection,
                                       String selection,
                                       String[] selectionArgs,
                                       String sortOrder) {
        Log.d(TAG, "TrackingProvider.getEventsForPackage()");

        String packageId = TrackingContract.PackagesEntry.getPackageIdFromUri(uri);

        // build the selection string
        //packages._ID = ?
        if (null == selection) {
            selection = "";
        }
        if (!"".equals(selection)) {
            selection = selection + " AND ";
        }
        selection = selection
                + "("
                + TrackingContract.PackagesEntry.TABLE_NAME
                + "."
                + TrackingContract.PackagesEntry._ID + " = ?)";
        Log.d(TAG, selection);

        // copy the array of arguments
        int selectionArgsLength = null == selectionArgs ? 0 : selectionArgs.length;
        String[] newSelectionArgs = new String[selectionArgsLength + 1];
        if (selectionArgsLength > 0) {
            System.arraycopy(selectionArgs, 0, newSelectionArgs, 0, selectionArgsLength);
        }
        newSelectionArgs[selectionArgsLength] = packageId;

        return sEventsByPackageQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                newSelectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getLatestEventForPackages(String[] projection,
                                             String selection,
                                             String[] selectionArgs,
                                             String sortOrder) {
        Log.d(TAG, "TrackingProvider.getLatestEventForPackages()");

        // build the selection string
        // (usps_order = 0)
        if (null == selection) {
            selection = "";
        }
        if (!"".equals(selection)) {
            selection = selection + " AND ";
        }
        selection = selection
                + "("
                + TrackingContract.EventsEntry.COLUMN_EVENT_ORDER
                + " = 0)";
        Log.d(TAG, selection);

        return sEventsByPackageQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private void normalizeEventDate(ContentValues values) {
        Log.d(TAG, "TrackingProvider.normalizeEventDate()");

        String eventType = values.containsKey(TrackingContract.EventsEntry.COLUMN_TYPE) ?
                values.getAsString(TrackingContract.EventsEntry.COLUMN_TYPE)
                :
                "<event type column not found>";
        Log.d(TAG, "Event type: " + eventType);

        if (eventType.equals(TrackingContract.EventsEntry.TYPE_EVENT)) {
            // normalize the date value
            String dateValue = values.containsKey(TrackingContract.EventsEntry.COLUMN_DATE) ?
                    values.getAsString(TrackingContract.EventsEntry.COLUMN_DATE) : "";
            Log.d(TAG, "Date: " + dateValue);

            String timeValue = values.containsKey(TrackingContract.EventsEntry.COLUMN_TIME) ?
                    values.getAsString(TrackingContract.EventsEntry.COLUMN_TIME) : "";
            Log.d(TAG, "Time: " + timeValue);

            values.put(TrackingContract.EventsEntry.COLUMN_TIMESTAMP,
                    TrackingContract.normalizeDate(dateValue, timeValue));
        }
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "TrackingProvider.onCreate()");

        mOpenHelper = new TrackingDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "TrackingProvider.getType()");

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PACKAGES:
                return TrackingContract.PackagesEntry.CONTENT_TYPE;
            case PACKAGE_WITH_EVENTS:
                return TrackingContract.PackagesEntry.CONTENT_ITEM_TYPE;
            case PACKAGES_WITH_LATEST_EVENT:
                return TrackingContract.PackagesEntry.CONTENT_TYPE;
            case EVENTS:
                return TrackingContract.EventsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        Log.d(TAG, "TrackingProvider.query()");

        final int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case PACKAGES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TrackingContract.PackagesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                retCursor.setNotificationUri(getContext().getContentResolver(), uri);

                break;
            }
            case PACKAGE_WITH_EVENTS: {
                retCursor = getEventsForPackage(uri,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder);
                retCursor.setNotificationUri(getContext().getContentResolver(),
                        TrackingContract.BASE_CONTENT_URI);

                break;
            }
            case PACKAGES_WITH_LATEST_EVENT: {
                retCursor = getLatestEventForPackages(projection,
                        selection,
                        selectionArgs,
                        sortOrder);
                retCursor.setNotificationUri(getContext().getContentResolver(),
                        TrackingContract.BASE_CONTENT_URI);

                break;
            }
            case EVENTS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TrackingContract.EventsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                retCursor.setNotificationUri(getContext().getContentResolver(), uri);

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Log.d(TAG, "Returning records: " + Integer.toString(retCursor.getCount()));
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "TrackingProvider.insert()");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri retUri;

        switch (match) {
            case EVENTS: {
                printDatabase("before insert EVENTS");

                normalizeEventDate(values);
                long _id = db.insert(TrackingContract.EventsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    retUri = TrackingContract.EventsEntry.buildEventUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                printDatabase("after insert EVENTS");

                break;
            }
            case PACKAGES: {
                printDatabase("before insert PACKAGES");

                long _id = db.insert(TrackingContract.PackagesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    retUri = TrackingContract.PackagesEntry.buildPackageUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                printDatabase("after insert PACKAGES");

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        updateRemotes();

        Log.d(TAG, retUri.toString());
        return retUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.d(TAG, "TrackingProvider.bulkInsert()");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case EVENTS: {
                printDatabase("before bulkInsert EVENTS");

                db.beginTransaction();
                int retCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeEventDate(value);
                        long _id = db.insert(TrackingContract.EventsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            retCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                updateRemotes();

                printDatabase("after bulkInsert EVENTS");

                return retCount;
            }
            default:
                printDatabase("before/after bulkInsert default");
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "TrackingProvider.delete()");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int retDeleted;

        // this makes delete all rows return the number of rows deleted
        if ( (null == selection) || "".equals(selection))
            selection = "1";

        switch (match) {
            case EVENTS:
                printDatabase("before delete EVENTS");

                retDeleted = db.delete(
                        TrackingContract.EventsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PACKAGES:
                printDatabase("before delete PACKAGES");

                retDeleted = db.delete(
                        TrackingContract.PackagesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                printDatabase("before delete default");

                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (retDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            updateRemotes();
        }

        printDatabase("after delete");

        return retDeleted;
    }

    private void updateRemotes() {
        Context context = getContext();

        Intent dataUpdatedIntent = new Intent(StillInMemphisSyncAdapter.ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "TrackingProvider.update()");

        printDatabase("before update");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int retUpdated;

        switch (match) {
            case EVENTS:
                normalizeEventDate(values);
                retUpdated = db.update(
                        TrackingContract.EventsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PACKAGES:
                retUpdated = db.update(
                        TrackingContract.PackagesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (retUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            updateRemotes();
        }

        printDatabase("after update");

        return retUpdated;
    }

    private void printDatabase(String method) {
        Log.d(TAG_PRINT_DATABASE, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Cursor packageCursor = query(TrackingContract.PackagesEntry.CONTENT_URI,
                null /*projection*/,
                null /*selection*/,
                null /*selection args*/,
                null /*sort order */);
        if (packageCursor.moveToFirst()) {
            Log.d(TAG_PRINT_DATABASE, "PACKAGES " + method);

            do {
                int packageIdIndex = packageCursor.getColumnIndex(TrackingContract.PackagesEntry._ID);
                int packageNumberIndex = packageCursor.getColumnIndex(TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER);
                int packageDescriptionIndex = packageCursor.getColumnIndex(TrackingContract.PackagesEntry.COLUMN_DESCRIPTION);

                Log.d(TAG_PRINT_DATABASE, String.format("%s: %s (%s)",
                        packageCursor.getString(packageIdIndex),
                        packageCursor.getString(packageNumberIndex),
                        packageCursor.getString(packageDescriptionIndex)));
            } while (packageCursor.moveToNext());
        } else {
            Log.d(TAG_PRINT_DATABASE, "PACKAGES " + method + " EMPTY");
        }
        packageCursor.close();

        Log.d(TAG_PRINT_DATABASE, "");

        Cursor eventsCursor = query(TrackingContract.EventsEntry.CONTENT_URI,
                null /*projection*/,
                null /*selection*/,
                null /*selection args*/,
                null /*sort order */);
        if (eventsCursor.moveToFirst()) {
            Log.d(TAG_PRINT_DATABASE, "EVENTS " + method);

            do {
                int eventIdIndex = eventsCursor.getColumnIndex(TrackingContract.EventsEntry._ID);
                int eventOrderIndex = eventsCursor.getColumnIndex(TrackingContract.EventsEntry.COLUMN_EVENT_ORDER);
                int eventPackageIdIndex = eventsCursor.getColumnIndex(TrackingContract.EventsEntry.COLUMN_PACKAGE_ID);
                int eventDescriptionIndex = eventsCursor.getColumnIndex(TrackingContract.EventsEntry.COLUMN_EVENT);

                Log.d(TAG_PRINT_DATABASE, String.format("%s (%s): [%s] %s",
                        eventsCursor.getString(eventIdIndex),
                        eventsCursor.getString(eventOrderIndex),
                        eventsCursor.getString(eventPackageIdIndex),
                        eventsCursor.getString(eventDescriptionIndex)));
            } while (eventsCursor.moveToNext());
        } else {
            Log.d(TAG_PRINT_DATABASE, "EVENTS " + method + " EMPTY");
        }
        eventsCursor.close();
    }

    private void printCursor(String method, Cursor cursor) {
        if (cursor.moveToFirst()) {
            Log.d(TAG_PRINT_DATABASE, "CURSOR " + method);

            do {
                int packageIdIndex = cursor.getColumnIndex(TrackingContract.PackagesEntry._ID);
                int packageNumberIndex = cursor.getColumnIndex(TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER);
                int packageDescriptionIndex = cursor.getColumnIndex(TrackingContract.PackagesEntry.COLUMN_DESCRIPTION);

                Log.d(TAG_PRINT_DATABASE, String.format("%s: %s (%s)",
                        cursor.getString(packageIdIndex),
                        cursor.getString(packageNumberIndex),
                        cursor.getString(packageDescriptionIndex)));
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG_PRINT_DATABASE, "CURSOR " + method + " EMPTY");
        }

    }
}
