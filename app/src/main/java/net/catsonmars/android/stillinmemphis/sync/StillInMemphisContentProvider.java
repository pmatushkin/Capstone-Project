package net.catsonmars.android.stillinmemphis.sync;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by pmatushkin on 4/22/2016.
 */
public class StillInMemphisContentProvider extends ContentProvider {
    private static final String TAG = "MemphisContentProvider";

    @Override
    public boolean onCreate() {
        Log.d(TAG, "StillInMemphisContentProvider.onCreate()");

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "StillInMemphisContentProvider.query()");

        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "StillInMemphisContentProvider.getType()");

        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "StillInMemphisContentProvider.insert()");

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "StillInMemphisContentProvider.delete()");

        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "StillInMemphisContentProvider.update()");

        return 0;
    }
}
