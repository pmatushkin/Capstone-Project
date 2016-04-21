package net.catsonmars.android.stillinmemphis.data;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


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

    String url = "http://production.shippingapis.com/ShippingAPI.dll?API=TrackV2&XML=<TrackFieldRequest USERID=\"\"><TrackID ID=\"\"></TrackID></TrackFieldRequest>";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            Log.d(TAG, run());
        } catch (IOException io) {
            Log.e(TAG, io.toString());
        }

        sendBroadcast(new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
        sendBroadcast(new Intent(BROADCAST_ACTION_DATA_CHANGE));
    }

    String run(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String run() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Uri.Builder builder = new Uri.Builder();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }

        return response.body().string();
    }
}
