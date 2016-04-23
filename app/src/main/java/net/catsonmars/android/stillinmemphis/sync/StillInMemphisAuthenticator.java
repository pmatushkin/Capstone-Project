package net.catsonmars.android.stillinmemphis.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by pmatushkin on 4/21/2016.
 */
public class StillInMemphisAuthenticator extends AbstractAccountAuthenticator {
    private static final String TAG = "MemphisAuthenticator";

    public StillInMemphisAuthenticator(Context context) {
        super(context);

        Log.d(TAG, "StillInMemphisAuthenticator.StillInMemphisAuthenticator()");
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.d(TAG, "StillInMemphisAuthenticator.editProperties()");

        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "StillInMemphisAuthenticator.addAccount()");

        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "StillInMemphisAuthenticator.confirmCredentials()");

        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "StillInMemphisAuthenticator.getAuthToken()");

        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.d(TAG, "StillInMemphisAuthenticator.getAuthTokenLabel()");

        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "StillInMemphisAuthenticator.updateCredentials()");

        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        Log.d(TAG, "StillInMemphisAuthenticator.hasFeatures()");

        return null;
    }
}
