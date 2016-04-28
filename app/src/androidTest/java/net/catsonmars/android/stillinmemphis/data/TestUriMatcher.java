package net.catsonmars.android.stillinmemphis.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by pmatushkin on 4/26/2016.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final long LOCATION_QUERY = 1L;

    // content://net.catsonmars.android.stillinmemphis/packages
    private static final Uri TEST_PACKAGES_DIR = TrackingContract.PackagesEntry.CONTENT_URI;

    // content://net.catsonmars.android.stillinmemphis/events
    private static final Uri TEST_EVENTS_DIR = TrackingContract.EventsEntry.CONTENT_URI;
    private static final Uri TEST_EVENTS_WITH_PACKAGE_DIR = TrackingContract.EventsEntry.buildEventPackage(LOCATION_QUERY);

    public void testUriMatcher() {
        UriMatcher testMatcher = TrackingProvider.buildUriMatcher();

        assertEquals("Error: The PACKAGES URI was matched incorrectly.",
                testMatcher.match(TEST_PACKAGES_DIR), TrackingProvider.PACKAGES);

        assertEquals("Error: The EVENTS URI was matched incorrectly.",
                testMatcher.match(TEST_EVENTS_DIR), TrackingProvider.EVENTS);

        assertEquals("Error: The WEATHER WITH LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_EVENTS_WITH_PACKAGE_DIR), TrackingProvider.PACKAGES_WITH_EVENTS);
    }
}
