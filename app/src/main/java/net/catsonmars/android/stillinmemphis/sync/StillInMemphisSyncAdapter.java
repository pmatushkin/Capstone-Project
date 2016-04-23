package net.catsonmars.android.stillinmemphis.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by pmatushkin on 4/21/2016.
 */
public class StillInMemphisSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "MemphisSyncAdapter";

    public StillInMemphisSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        Log.d(TAG, "StillInMemphisSyncAdapter.StillInMemphisSyncAdapter( , )");
    }

    public StillInMemphisSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        Log.d(TAG, "StillInMemphisSyncAdapter.StillInMemphisSyncAdapter( , , )");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "StillInMemphisSyncAdapter.onPerformSync()");
    }
}
