package net.catsonmars.android.stillinmemphis.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StillInMemphisAuthenticatorService extends Service {
    private static final String TAG = "MemphisAuthService";

    private StillInMemphisAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        Log.d(TAG, "StillInMemphisAuthenticatorService.onCreate()");

        mAuthenticator = new StillInMemphisAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "StillInMemphisAuthenticatorService.onBind()");

        return mAuthenticator.getIBinder();
    }
}
