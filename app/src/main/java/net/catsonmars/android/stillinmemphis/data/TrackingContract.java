package net.catsonmars.android.stillinmemphis.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by pmatushkin on 4/24/2016.
 */
public class TrackingContract {
    private static final String TAG = "TrackingContract";

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "net.catsonmars.android.stillinmemphis";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_PACKAGES = "packages";
    public static final String PATH_EVENTS = "events";

    // JOIN string for the static initializer in content provider
    // This is an inner join which looks like
    // events INNER JOIN packages ON events.package_id = packages._id
    public static final String joinString =
            EventsEntry.TABLE_NAME + " INNER JOIN " + PackagesEntry.TABLE_NAME +
            " ON " + EventsEntry.TABLE_NAME + "." + EventsEntry.COLUMN_PACKAGE_ID +
            " = " + PackagesEntry.TABLE_NAME + "." + PackagesEntry._ID;

    public static String FORMAT_DATE_TIME = "MMMM dd, yyyy hh:mm a";
    public static String FORMAT_DATE = "MMMM dd, yyyy";
    public static String FORMAT_TIME = "hh:mm a";

    /**
     * This is the date/time logic:
     * - assumption: all USPS time stamps belong to the same time zone: UTC
     * - assumption: the app always operates in the same time zone as USPS: UTC
     * - assumption: all dates/times are combined and parsed into a single UTC date/time value, which is then stored
     * To build this single UTC date/time value:
     * - Use a combination of EventTime and EventDate
     * - If EventDate is there, but EventTime is missing, use midnight of EventDate
     * - If EventTime is there, but EventDate is missing, use today's date + EventTime
     * - If missing completely, use current time
     * @param dateValue Date part of a resulting date/time
     * @param timeValue Time part of a resulting date/time
     * @return normalized date/time in milliseconds
     */
    // http://stackoverflow.com/questions/308683/how-can-i-get-the-current-date-and-time-in-utc-or-gmt-in-java/6697884#6697884
    public static long normalizeDate(String dateValue, String timeValue) {
        Log.d(TAG, "normalizeDate()");

        Date currentDate = new Date();

        // http://developer.android.com/reference/java/text/SimpleDateFormat.html
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME, Locale.US);
        sdf.setLenient(false);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        SimpleDateFormat sdfDate = new SimpleDateFormat(FORMAT_DATE, Locale.US);
        sdfDate.setLenient(false);
        sdfDate.setTimeZone(TimeZone.getTimeZone("GMT"));

        SimpleDateFormat sdfTime = new SimpleDateFormat(FORMAT_TIME, Locale.US);
        sdfTime.setLenient(false);
        sdfTime.setTimeZone(TimeZone.getTimeZone("GMT"));

        if ((null == dateValue) || "".equals(dateValue)) {
            Log.d(TAG, "dateValue is missing");

            dateValue = sdfDate.format(currentDate);
        }
        Log.d(TAG, "dateValue: " + dateValue);

        if ((null == timeValue) || "".equals(timeValue)) {
            Log.d(TAG, "timeValue is missing");

            Date midnightTime = new Date(0);
            timeValue = sdfTime.format(midnightTime);
        }
        Log.d(TAG, "timeValue: " + timeValue);

        String dateString = dateValue + " " + timeValue;
        Log.d(TAG, "dateString: " + dateString);

        long retLong = 0;

        try {
            Date eventDate = sdf.parse(dateString);
            retLong = eventDate.getTime();
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
        }

        Log.d(TAG, "retLong: " + retLong);
        return retLong;
    }

    public static final class PackagesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PACKAGES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PACKAGES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PACKAGES;

        // Table name
        public static final String TABLE_NAME = "packages";

        // Tracking number
        // Display this as a package title if the description is not entered. Use it when running a sync.
        public static final String COLUMN_TRACKING_NUMBER = "tracking_number";

        // Package description
        // Display this as a package title if entered. Display the tracking number as a subtitle.
        public static final String COLUMN_DESCRIPTION = "description";

        // Is package archived?
        // When 1, display the package on Archive list, do not sync.
        // When 0, display the package on the main list, do sync.
        public static final String COLUMN_ARCHIVED = "archived";

        // When the package was added?
        // Currently not used. Future purpose: sorting by date added.
        public static final String COLUMN_DATE_ADDED = "date_added";

        // When the package was delivered?
        // When set, use it to automatically archive the packages delivered more than one week ago.
        // Exclude delivered packages from sync.
        // The package becomes delivered when TrackSummary.Event contains the word "delivered"
        public static final String COLUMN_DATE_DELIVERED = "date_delivered";

        // a single package by id
        // packages/#
        public static Uri buildPackageUri(long id) {
            Log.d(TAG, "PackagesEntry.buildPackageUri()");

            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // a single package by id with all events
        // packages/#/*
        public static Uri buildPackageWithEventsUri(long id) {
            Log.d(TAG, "PackagesEntry.buildPackageWithEventsUri()");

            return ContentUris.withAppendedId(CONTENT_URI, id).buildUpon().appendPath("*").build();
        }

        // all packages with the latest event
        // packages/*/0
        public static Uri buildPackagesWithLatestEventUri() {
            Log.d(TAG, "PackagesEntry.buildPackagesWithLatestEventUri()");

            return CONTENT_URI.buildUpon().appendPath("*").appendPath("0").build();
        }

        public static String getPackageIdFromUri(Uri uri) {
            Log.d(TAG, "PackagesEntry.getPackageIdFromUri()");

            return uri.getPathSegments().get(1);
        }
    }

    public static final class EventsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;

        // event type strings
        public static final String TYPE_EVENT = "event";
        public static final String TYPE_ERROR = "error";

        // Table name
        public static final String TABLE_NAME = "events";

        // package id
        // Foreign key for the package table.
        public static final String COLUMN_PACKAGE_ID = "package_id";

        // Event order
        // The events are ordered by the USPS web service. This is the event number in this order.
        // Use it to sort the events when displaying them on the Details page.
        public static final String COLUMN_EVENT_ORDER = "usps_order";

        // Event type
        // "detail" or "error"
        public static final String COLUMN_TYPE = "type";

        // Event time stamp
        // Use it to sort the events by Newest First on the main page. Don't display.
        // For error events, always use the smallest value to display them at the bottom.
        public static final String COLUMN_TIMESTAMP = "timestamp";

        // The contents of EventTime node
        // Use to display the event time stamp.
        public static final String COLUMN_TIME = "time";

        // The contents of EventDate node
        // Use to display the event time stamp.
        public static final String COLUMN_DATE = "date";

        // The contents of Event node
        // Use to display the event description.
        public static final String COLUMN_EVENT = "event";

        // The contents of EventCity node
        // Use to display the event location.
        public static final String COLUMN_CITY = "city";

        // The contents of EventState node
        // Use to display the event location.
        public static final String COLUMN_STATE = "state";

        // The contents of EventZIPCode node
        // Use to display the event location.
        public static final String COLUMN_ZIP = "zip";

        // The contents of EventCountry node
        // Use to display the event location.
        public static final String COLUMN_COUNTRY = "country";

        // a single event by id
        // events/#
        public static Uri buildEventUri(long id) {
            Log.d(TAG, "EventsEntry.buildEventUri()");

            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
