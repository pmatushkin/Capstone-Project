package net.catsonmars.android.stillinmemphis.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by pmatushkin on 4/24/2016.
 */
public class TrackingProvider extends ContentProvider {
    private static final String TAG = "TrackingProvider";

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

    private Cursor getEventsForPackage(Uri uri, String[] projection, String sortOrder) {
        Log.d(TAG, "TrackingProvider.getEventsForPackage()");

        String packageId = TrackingContract.PackagesEntry.getPackageIdFromUri(uri);

        //packages._ID = ?
        String selection = TrackingContract.PackagesEntry.TABLE_NAME
                + "."
                + TrackingContract.PackagesEntry._ID + " = ?";
        Log.d(TAG, selection);
        String[] selectionArgs = new String[] { packageId };

        return sEventsByPackageQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getLatestEventForPackages(String[] projection, String sortOrder) {
        Log.d(TAG, "TrackingProvider.getLatestEventForPackages()");

        // only active packages, only events where order==0
        // ((archived = 0) AND (usps_order = 0))
        String selection = "((" + TrackingContract.PackagesEntry.COLUMN_ARCHIVED + " = 0) AND ("
                + TrackingContract.EventsEntry.COLUMN_EVENT_ORDER + " = 0))";
        Log.d(TAG, selection);

        return sEventsByPackageQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                null,
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
                retCursor = getEventsForPackage(uri, projection, sortOrder);
                retCursor.setNotificationUri(getContext().getContentResolver(), TrackingContract.BASE_CONTENT_URI);

                break;
            }
            case PACKAGES_WITH_LATEST_EVENT: {
                retCursor = getLatestEventForPackages(projection, sortOrder);
                retCursor.setNotificationUri(getContext().getContentResolver(), TrackingContract.BASE_CONTENT_URI);

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
                normalizeEventDate(values);
                long _id = db.insert(TrackingContract.EventsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    retUri = TrackingContract.EventsEntry.buildEventUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PACKAGES: {
                long _id = db.insert(TrackingContract.PackagesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    retUri = TrackingContract.PackagesEntry.buildPackageUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

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

                return retCount;
            }
            default:
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
                retDeleted = db.delete(
                        TrackingContract.EventsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PACKAGES:
                retDeleted = db.delete(
                        TrackingContract.PackagesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (retDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return retDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "TrackingProvider.update()");

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
        }

        return retUpdated;
    }
}
