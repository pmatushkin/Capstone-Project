package net.catsonmars.android.stillinmemphis.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import net.catsonmars.android.stillinmemphis.R;

public class StillInMemphisSyncService extends Service {
    private static final String TAG = "MemphisSyncService";

    // Interval at which to sync with the USPS web service, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final Object sSyncAdapterLock = new Object();
    private static StillInMemphisSyncAdapter sStillInMemphisSyncAdapter = null;

    public StillInMemphisSyncService() {
        Log.d(TAG, "StillInMemphisSyncService.StillInMemphisSyncService()");
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        Log.d(TAG, "StillInMemphisSyncService.onCreate()");

        synchronized (sSyncAdapterLock) {
            if (sStillInMemphisSyncAdapter == null) {
                sStillInMemphisSyncAdapter = new StillInMemphisSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "StillInMemphisSyncService.onBind()");

        return sStillInMemphisSyncAdapter.getSyncAdapterBinder();
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        Log.d(TAG, "StillInMemphisSyncService.getSyncAccount()");

        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void initializeSyncAdapter(Context context) {
        Log.d(TAG, "StillInMemphisSyncService.initializeSyncAdapter()");

        getSyncAccount(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Log.d(TAG, "StillInMemphisSyncService.configurePeriodicSync()");

        Account account = getSyncAccount(context);

        String authority = context.getString(R.string.content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();

            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(TAG, "StillInMemphisSyncService.syncImmediately()");

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        Account account = getSyncAccount(context);
        Log.d(TAG, "requesting sync...");
        ContentResolver.requestSync(account,
                context.getString(R.string.content_authority),
                bundle);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediatelyWithTrackingNumber(Context context, String trackingNumber) {
        Log.d(TAG, "StillInMemphisSyncService.syncImmediately()");

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putString(StillInMemphisSyncAdapter.EXTRA_TRACKING_NUMBER, trackingNumber);

        Account account = getSyncAccount(context);
        Log.d(TAG, "requesting sync...");
        ContentResolver.requestSync(account,
                context.getString(R.string.content_authority),
                bundle);
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        Log.d(TAG, "StillInMemphisSyncService.onAccountCreated()");

        /*
         * Since we've created an account
         */
        StillInMemphisSyncService.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }
}
