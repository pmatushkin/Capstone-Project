package net.catsonmars.android.stillinmemphis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.catsonmars.android.stillinmemphis.sync.StillInMemphisSyncService;

/**
 * Created by pmatushkin on 5/19/2016.
 */
public class NetworkReceiver extends BroadcastReceiver {
    public static final String KEY_IS_NETWORK_AVAILABLE = "KEY_IS_NETWORK_AVAILABLE";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNetworkAvailable = !NetworkUtils.isAirplaneModeOn(context)
                && NetworkUtils.isNetworkAvailable(context);

        if (isNetworkAvailable) {
            StillInMemphisSyncService.syncImmediately(context);
        }
    }
}
