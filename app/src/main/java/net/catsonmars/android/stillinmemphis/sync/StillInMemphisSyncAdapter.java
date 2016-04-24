package net.catsonmars.android.stillinmemphis.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import net.catsonmars.android.stillinmemphis.BuildConfig;
import net.catsonmars.android.stillinmemphis.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    public static final String EXTRA_TRACKING_NNMBER
            = "net.catsonmars.android.stillinmemphis.intent.extra.TRACKING_NUMBER";

    // A maximum quantity of tracking numbers in a single request is 10
    private static final int MAX_TRACKING_NUMBERS_COUNT = 10;

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

        // get tracking numbers to sync
        String[] trackingNumbersToSync = extras.containsKey(EXTRA_TRACKING_NNMBER) ?
                getTrackingNumbersToSync(extras)
                :
                getTrackingNumbersToSync();

        if ((trackingNumbersToSync != null) && (trackingNumbersToSync.length > 0)) {
            int trackingNumberPosition = 0;

            while (trackingNumberPosition < trackingNumbersToSync.length) {
                int trackingNumberCount = 1;
                Log.d(TAG, "NEXT REQUEST");

                // build a request header
                // how to build an XML document: http://stackoverflow.com/questions/6547263/how-to-make-xml-document-from-string-in-java-or-android
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = null;
                try {
                    documentBuilder = documentBuilderFactory.newDocumentBuilder();
                } catch (ParserConfigurationException pcfg_ex) {
                    Log.e(TAG, pcfg_ex.toString());
                }
                if (documentBuilder != null) {
                    Document document = documentBuilder.newDocument();

                    Element root = document.createElement("TrackFieldRequest");
                    root.setAttribute("USERID", BuildConfig.USPS_WEB_TOOLS_API_KEY);
                    document.appendChild(root);

                    Element trackIDElement;

                    // add next (no more than) MAX_TRACKING_NUMBERS_COUNT tracking numbers
                    while ((trackingNumberCount <= MAX_TRACKING_NUMBERS_COUNT) && (trackingNumberPosition < trackingNumbersToSync.length)) {
                        trackIDElement = document.createElement("TrackID");
                        trackIDElement.setAttribute("ID", trackingNumbersToSync[trackingNumberPosition]);
                        root.appendChild(trackIDElement);

                        trackingNumberCount++;
                        trackingNumberPosition++;
                    }

                    // build the request string
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = null;
                    try {
                        transformer = transformerFactory.newTransformer();
                    } catch (TransformerConfigurationException tcfg_ex) {
                        Log.e(TAG, tcfg_ex.toString());
                    }
                    if (transformer != null) {
                        DOMSource source = new DOMSource(document);

                        OutputStream outputStream = new ByteArrayOutputStream();
                        StreamResult streamResult = new StreamResult(outputStream);
                        try {
                            transformer.transform(source, streamResult);
                        } catch (TransformerException t_ex) {
                            Log.e(TAG, t_ex.toString());
                        }
                        try {
                            outputStream.close();
                        } catch (IOException io_ex) {
                            Log.e(TAG, io_ex.toString());
                        }

                        String requestString = outputStream.toString();
                        Log.d(TAG, requestString);

                        // build the API url
                        Context context = getContext();
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme(context.getString(R.string.usps_scheme))
                                .authority(context.getString(R.string.usps_authority))
                                .appendPath(context.getString(R.string.usps_path))
                                .appendQueryParameter(context.getString(R.string.usps_api_parameter), context.getString(R.string.usps_api_value))
                                .appendQueryParameter(context.getString(R.string.usps_xml_parameter), requestString);
                        Uri uri = builder.build();
                        String url = uri.toString();
                        Log.d(TAG, url);

                        // send the request
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(url)
                                .build();

                        try {
                            Response response = client.newCall(request).execute();

                            if (response != null) {
                                // read a response
                                String responseString = response.body().string();
                                Log.d(TAG, responseString);

                                // parse the response
                                document = documentBuilder.parse(new InputSource(new StringReader(responseString)));

                                // process the response
                                NodeList errorElements = document.getElementsByTagName("Error");
                                if (errorElements.getLength() > 0) {
                                    Node errorElement = errorElements.item(0);
                                    errorElement.getChildNodes();
                                }

                            }
                        } catch (IOException io_ex) {
                            Log.e(TAG, io_ex.toString());
                        } catch (SAXException sax_ex) {

                        }
                    }
                }
            }
        }

        // complete sync
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

    /**
     * Extracts tracking number from a bundle
     * @param bundle The bundle that stores the tracking number
     * @return String array with tracking number
     */
    private String[] getTrackingNumbersToSync(Bundle bundle)
    {
        Log.d(TAG, "StillInMemphisSyncAdapter.getTrackingNumbersToSync(Bundle)");

        if (bundle.containsKey(EXTRA_TRACKING_NNMBER)) {
            String[] a = new String[1];
            a[0] = bundle.getString(EXTRA_TRACKING_NNMBER);

            return a;
        } else {
            return new String[0];
        }
    }

    private String[] getTrackingNumbersToSync()
    {
        Log.d(TAG, "StillInMemphisSyncAdapter.getTrackingNumbersToSync()");
        Log.d(TAG, "return the array of active tracking numbers from the database");

        String[] a = new String[100];

        for (int i = 0; i < a.length; i++){
            a[i] = Integer.toString(i);
        }

        return a;
    }
}
