package net.catsonmars.android.stillinmemphis.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import net.catsonmars.android.stillinmemphis.BuildConfig;
import net.catsonmars.android.stillinmemphis.R;
import net.catsonmars.android.stillinmemphis.data.TrackingContract;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.Vector;

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

import static net.catsonmars.android.stillinmemphis.XmlUtils.asList;

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

    // A maximum number of entries in a single tracking request is 10
    private static final int MAX_TRACKING_NUMBERS_COUNT = 10;

    private Date mErrorTimestamp;

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

                                processUSPSResponseString(responseString);
                            }
                        } catch (IOException ex) {
                            Log.e(TAG, ex.toString());
                        }
                    }
                }
            }
        }

        // complete sync
        onSyncCompleted();
    }

    public void processUSPSResponseString(String responseString) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException pcfg_ex) {
            Log.e(TAG, pcfg_ex.toString());
        }

        if (documentBuilder != null) {
            try {
                // parse the response
                Document document = documentBuilder.parse(new InputSource(new StringReader(responseString)));

                // For error events, always use the smallest Date value to display them at the bottom of the list.
                // So we initialize it here, before any processing takes place.
                mErrorTimestamp = new Date();

                processUSPSResponseDocument(document);
            } catch (IOException | SAXException ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }

    private void processUSPSResponseDocument(Document document) {
        // process the response
        NodeList trackInfoElements = document.getElementsByTagName("TrackInfo");
        if (0 == trackInfoElements.getLength()) {
            // exit
            // for some reason the response doesn't have any data nodes
        } else {
            for (Node trackInfoElement : asList(trackInfoElements)) {
                processTrackInfoElement(trackInfoElement);
            }
        }
    }

    private void processTrackInfoElement(Node trackInfoElement) {
        NamedNodeMap trackInfoAttributes = trackInfoElement.getAttributes();

        Node idAttributeNode = trackInfoAttributes.getNamedItem("ID");
        if (null == idAttributeNode) {
            // exit
            // for some reason this TrackInfo element doesn't have a package tracking number
        } else {
            String trackingNumber = idAttributeNode.getNodeValue();
            Log.d(TAG, "tracking number " + trackingNumber);

            long packageId = addPackage(trackingNumber);

            NodeList trackDetailElements = trackInfoElement.getChildNodes();
            Log.d(TAG, "found " + trackDetailElements.getLength() + " track detail elements");

            int trackDetailElementsCount = trackDetailElements.getLength();

            if (0 == trackDetailElementsCount) {
                // exit
                // for some reason the TrackInfo element doesn't have any detail nodes
            } else {
                int trackDetailElementIndex = 0;

                Vector<ContentValues> cvVector = new Vector<>(trackDetailElementsCount);

                while (trackDetailElementIndex < trackDetailElementsCount) {
                    Node trackDetailElement = trackDetailElements.item(trackDetailElementIndex);

                    if (trackDetailElement.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList eventElements = trackDetailElement.getChildNodes();

                        if (0 == eventElements.getLength()) {
                            // exit
                            // for some reason the event element doesn't have any detail nodes
                        } else {
                            String trackDetailType = ((Element) trackDetailElement).getTagName();

                            ContentValues cv;
                            if ("Error".equals(trackDetailType)) {
                                Log.d(TAG, trackDetailType + " is an error event");

                                cv = processErrorEvent(packageId, eventElements);
                            } else {
                                Log.d(TAG, trackDetailType + " is a regular event");

                                cv = processRegularEvent(packageId,
                                        trackDetailElementIndex,
                                        eventElements);
                            }

                            if (cv != null) {
                                cvVector.add(cv);
                            }
                        }
                    }

                    trackDetailElementIndex++;
                }

                if (cvVector.size() > 0 ) {
                    ContentValues[] cvArray = new ContentValues[cvVector.size()];
                    cvVector.toArray(cvArray);
                    getContext()
                            .getContentResolver()
                            .bulkInsert(TrackingContract.EventsEntry.CONTENT_URI, cvArray);

                    // TODO update notifications here
                }
            }
        }
    }

    private ContentValues processErrorEvent(long packageId, NodeList eventElements) {
        Log.d(TAG, "StillInMemphisSyncAdapter.processErrorEvent()");

        String description = null;

        for (Node eventElement : asList(eventElements)) {
            if ((eventElement.getNodeType() == Node.ELEMENT_NODE)
                    && "Description".equals(((Element) eventElement).getTagName() )) {

                description = eventElement.getTextContent();
            }
        }

        ContentValues retValues = null;

        if (null != description) {
            retValues = new ContentValues();

            retValues.put(TrackingContract.EventsEntry.COLUMN_PACKAGE_ID, packageId);
            // event order is always 0 for the error event (this is the only event for this package)
            retValues.put(TrackingContract.EventsEntry.COLUMN_EVENT_ORDER, 0);
            retValues.put(TrackingContract.EventsEntry.COLUMN_TYPE,
                    TrackingContract.EventsEntry.TYPE_ERROR);
            retValues.put(TrackingContract.EventsEntry.COLUMN_TIMESTAMP, mErrorTimestamp.getTime());
            retValues.put(TrackingContract.EventsEntry.COLUMN_EVENT, description);
        }

        return retValues;
    }

    private ContentValues processRegularEvent(long packageId,
                                              int trackDetailElementIndex,
                                              NodeList eventElements) {
        Log.d(TAG, "StillInMemphisSyncAdapter.processRegularEvent()");

        String time = null;
        String date = null;
        String description = null;
        String city = null;
        String state = null;
        String zip = null;
        String country = null;

        Log.d(TAG, "index: " + trackDetailElementIndex);

        for (Node eventElement : asList(eventElements)) {
            if (eventElement.getNodeType() == Node.ELEMENT_NODE) {
                String tagName = ((Element) eventElement).getTagName();
                String textContent = eventElement.getTextContent();

                if ("EventTime".equals(tagName)) {
                    Log.d(TAG, "time: " + textContent);
                    time = textContent;
                } else if ("EventDate".equals(tagName)) {
                    Log.d(TAG, "date: " + textContent);
                    date = textContent;
                } else if ("Event".equals(tagName)) {
                    Log.d(TAG, "description: " + textContent);
                    description = textContent;
                } else if ("EventCity".equals(tagName)) {
                    Log.d(TAG, "city: " + textContent);
                    city = textContent;
                } else if ("EventState".equals(tagName)) {
                    Log.d(TAG, "state: " + textContent);
                    state = textContent;
                } else if ("EventZIPCode".equals(tagName)) {
                    Log.d(TAG, "zip: " + textContent);
                    zip = textContent;
                } else if ("EventCountry".equals(tagName)) {
                    Log.d(TAG, "country: " + textContent);
                    country = textContent;
                }
            }
        }

        ContentValues retValues = null;

        if (null != description) {
            retValues = new ContentValues();

            retValues.put(TrackingContract.EventsEntry.COLUMN_PACKAGE_ID, packageId);
            retValues.put(TrackingContract.EventsEntry.COLUMN_EVENT_ORDER, trackDetailElementIndex);
            retValues.put(TrackingContract.EventsEntry.COLUMN_TYPE, TrackingContract.EventsEntry.TYPE_EVENT);
            // No need to do it here, because the date will be normalized,
            // and the column updated, in bulkInsert().
            //long normalizedDate = TrackingContract.normalizeDate(date, time);
            //retValues.put(TrackingContract.EventsEntry.COLUMN_TIMESTAMP, normalizedDate);
            if (null != time)
                retValues.put(TrackingContract.EventsEntry.COLUMN_TIME, time);
            if (null != date)
                retValues.put(TrackingContract.EventsEntry.COLUMN_DATE, date);
            retValues.put(TrackingContract.EventsEntry.COLUMN_EVENT, description);
            if (null != city)
                retValues.put(TrackingContract.EventsEntry.COLUMN_CITY, city);
            if (null != state)
                retValues.put(TrackingContract.EventsEntry.COLUMN_STATE, state);
            if (null != zip)
                retValues.put(TrackingContract.EventsEntry.COLUMN_ZIP, zip);
            if (null != country)
                retValues.put(TrackingContract.EventsEntry.COLUMN_COUNTRY, country);
        }

        return retValues;
    }

    private long addPackage(String trackingNumber) {
        Log.d(TAG, "StillInMemphisSyncAdapter.addPackage()");

        long packageId;

        // First, check if the package with this tracking number exists in the db
        Cursor packageCursor = getContext().getContentResolver().query(
                TrackingContract.PackagesEntry.CONTENT_URI,
                new String[] { TrackingContract.PackagesEntry._ID },
                TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER + " = ?",
                new String[] { trackingNumber },
                null);

        if (packageCursor.moveToFirst()) {
            Log.d(TAG, "tracking number " + trackingNumber + " is found in the table");

            int packageIdIndex = packageCursor.getColumnIndex(TrackingContract.PackagesEntry._ID);
            packageId = packageCursor.getLong(packageIdIndex);

            deleteAllPackageEvents(packageId);
        } else {
            Log.d(TAG, "tracking number " + trackingNumber + " is NOT found in the table");

            ContentValues packageValues = new ContentValues();

            Date currentDate = new Date();

            packageValues.put(TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER, trackingNumber);
            packageValues.put(TrackingContract.PackagesEntry.COLUMN_ARCHIVED, 0);
            packageValues.put(TrackingContract.PackagesEntry.COLUMN_DATE_ADDED, currentDate.getTime());

            // Finally, insert location data into the database.
            Uri insertedUri = getContext().getContentResolver().insert(
                    TrackingContract.PackagesEntry.CONTENT_URI,
                    packageValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            packageId = ContentUris.parseId(insertedUri);
        }

        Log.d(TAG, "package id: " + packageId);

        packageCursor.close();

        return packageId;
    }

    private void deleteAllPackageEvents(long packageId) {
        // Delete the previously inserted events for the package
        int deletedEvents = getContext().getContentResolver().delete(
                TrackingContract.EventsEntry.CONTENT_URI,
                TrackingContract.EventsEntry.COLUMN_PACKAGE_ID + " = ?",
                new String[] { String.valueOf(packageId) }
        );

        Log.d(TAG, "deleted events: " + deletedEvents);
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
        // TODO implement getTrackingNumbersToSync() for syncing a single tracking number
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
        // TODO implement getTrackingNumbersToSync()
        Log.d(TAG, "StillInMemphisSyncAdapter.getTrackingNumbersToSync()");
        Log.d(TAG, "return the array of active tracking numbers from the database");

        String[] a = new String[2];

//        a[0] = "9405503699300270004035";
        a[0] = "9405803699300222655286";
        a[1] = "0";

//        for (int i = 0; i < a.length; i++){
//            a[i] = Integer.toString(i);
//        }

        return a;
    }
}
