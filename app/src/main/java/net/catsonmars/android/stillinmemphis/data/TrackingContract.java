package net.catsonmars.android.stillinmemphis.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by pmatushkin on 4/24/2016.
 */
public class TrackingContract {

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

        public static Uri buildPackageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class EventsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;

        // Table name
        public static final String TABLE_NAME = "events";

        // package id
        // Foreign key for the package table.
        public static final String COLUMN_PACKAGE_ID = "package_id";

        // Event order
        // The events are ordered by the USPS web service. This is the event number in this order.
        // Use it to sort the events when displaying them on the Details page.
        public static final String COLUMN_EVENT_ORDER = "order";

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

        public static Uri buildPackageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
