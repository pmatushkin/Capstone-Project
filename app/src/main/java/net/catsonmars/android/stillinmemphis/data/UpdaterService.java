package net.catsonmars.android.stillinmemphis.data;

import android.app.IntentService;
import android.content.Intent;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "net.catsonmars.android.stillinmemphis.intent.action.STATE_CHANGE";
    public static final String BROADCAST_ACTION_DATA_CHANGE
            = "net.catsonmars.android.stillinmemphis.intent.action.DATA_CHANGE";
    public static final String EXTRA_REFRESHING
            = "net.catsonmars.android.stillinmemphis.intent.extra.REFRESHING";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sendBroadcast(new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
        sendBroadcast(new Intent(BROADCAST_ACTION_DATA_CHANGE));
    }
}
