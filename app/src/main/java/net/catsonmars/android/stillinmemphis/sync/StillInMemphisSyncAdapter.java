package net.catsonmars.android.stillinmemphis.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by pmatushkin on 4/21/2016.
 */
public class StillInMemphisSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "MemphisSyncAdapter";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "net.catsonmars.android.stillinmemphis.intent.action.STATE_CHANGE";
    public static final String BROADCAST_ACTION_DATA_CHANGE
            = "net.catsonmars.android.stillinmemphis.intent.action.DATA_CHANGE";
    public static final String EXTRA_REFRESHING
            = "net.catsonmars.android.stillinmemphis.intent.extra.REFRESHING";

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


        onSyncCompleted();
    }

    /**
     * Sends a broadcast message to signal the sync completion
     */
    private void onSyncCompleted() {
        Log.d(TAG, "StillInMemphisSyncAdapter.onSyncCompleted()");

        Context context = getContext();

        // Setting the package ensures that only components in our app will receive the broadcast
        Intent intent = new Intent(BROADCAST_ACTION_STATE_CHANGE)
                .putExtra(EXTRA_REFRESHING, false)
                .setPackage(context.getPackageName());

        context.sendBroadcast(intent);
    }
}
