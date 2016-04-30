package net.catsonmars.android.stillinmemphis.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import net.catsonmars.android.stillinmemphis.sync.StillInMemphisSyncAdapter;

/**
 * Created by pmatushkin on 4/30/2016.
 */
public class TestUSPSResponse extends AndroidTestCase {
    private final String responseString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<TrackResponse><TrackInfo ID=\"9405503699300270004035\"><TrackSummary><EventTime>2:21 pm</EventTime><EventDate>February 25, 2016</EventDate><Event>Delivered, In/At Mailbox</Event><EventCity>CORVALLIS</EventCity><EventState>OR</EventState><EventZIPCode>97333</EventZIPCode><EventCountry/><FirmName/><Name/><AuthorizedAgent>false</AuthorizedAgent><DeliveryAttributeCode>01</DeliveryAttributeCode></TrackSummary><TrackDetail><EventTime>9:06 am</EventTime><EventDate>February 25, 2016</EventDate><Event>Out for Delivery</Event><EventCity>CORVALLIS</EventCity><EventState>OR</EventState><EventZIPCode>97333</EventZIPCode><EventCountry/><FirmName/><Name/><AuthorizedAgent>false</AuthorizedAgent></TrackDetail><TrackDetail><EventTime>8:56 am</EventTime><EventDate>February 25, 2016</EventDate><Event>Sorting Complete</Event><EventCity>CORVALLIS</EventCity><EventState>OR</EventState><EventZIPCode>97333</EventZIPCode><EventCountry/><FirmName/><Name/><AuthorizedAgent>false</AuthorizedAgent></TrackDetail><TrackDetail><EventTime>6:55 am</EventTime><EventDate>February 25, 2016</EventDate><Event>Arrived at Post Office</Event><EventCity>CORVALLIS</EventCity><EventState>OR</EventState><EventZIPCode>97333</EventZIPCode><EventCountry/><FirmName/><Name/><AuthorizedAgent>false</AuthorizedAgent></TrackDetail><TrackDetail><EventTime>3:24 am</EventTime><EventDate>February 25, 2016</EventDate><Event>Departed USPS Facility</Event><EventCity>PORTLAND</EventCity><EventState>OR</EventState><EventZIPCode>97218</EventZIPCode><EventCountry/><FirmName/><Name/><AuthorizedAgent>false</AuthorizedAgent></TrackDetail><TrackDetail><EventTime>11:51 pm</EventTime><EventDate>February 24, 2016</EventDate><Event>Arrived at USPS Destination Facility</Event><EventCity>PORTLAND</EventCity><EventState>OR</EventState><EventZIPCode>97218</EventZIPCode><EventCountry/><FirmName/><Name/><AuthorizedAgent>false</AuthorizedAgent></TrackDetail><TrackDetail><EventTime>12:47 am</EventTime><EventDate>February 24, 2016</EventDate><Event>Arrived at USPS Origin Facility</Event><EventCity>ATLANTA</EventCity><EventState>GA</EventState><EventZIPCode>30320</EventZIPCode><EventCountry/><FirmName/><Name/><AuthorizedAgent>false</AuthorizedAgent></TrackDetail><TrackDetail><EventTime>11:32 pm</EventTime><EventDate>February 23, 2016</EventDate><Event>Accepted at USPS Origin Facility</Event><EventCity>ROSWELL</EventCity><EventState>GA</EventState><EventZIPCode>30076</EventZIPCode><EventCountry/><FirmName/><Name/><AuthorizedAgent>false</AuthorizedAgent></TrackDetail><TrackDetail><EventTime/><EventDate>February 23, 2016</EventDate><Event>Pre-Shipment Info Sent to USPS</Event><EventCity/><EventState/><EventZIPCode/><EventCountry/><FirmName/><Name/><AuthorizedAgent>false</AuthorizedAgent></TrackDetail></TrackInfo><TrackInfo ID=\"0\"><Error><Number>-2147219302</Number><Description>The Postal Service could not locate the tracking information for your request. Please verify your tracking number and try again later.</Description><HelpFile/><HelpContext/></Error></TrackInfo></TrackResponse>";

    void deleteDatabase() {
        mContext.deleteDatabase(TrackingDbHelper.DATABASE_NAME);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        deleteDatabase();
    }

    public void testProcessUSPSResponseString() {
        StillInMemphisSyncAdapter syncAdapter = new StillInMemphisSyncAdapter(getContext(), true);

        syncAdapter.processUSPSResponseString(responseString);

        // Now test the number of inserted records

        TrackingDbHelper dbHelper = new TrackingDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor packageCursor = db.query(
                TrackingContract.PackagesEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        Cursor eventCursor = db.query(
                TrackingContract.EventsEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        assertEquals("Packages", 2, packageCursor.getCount());
        packageCursor.close();

        assertEquals("Events", 10, eventCursor.getCount());
        eventCursor.close();
    }
}
