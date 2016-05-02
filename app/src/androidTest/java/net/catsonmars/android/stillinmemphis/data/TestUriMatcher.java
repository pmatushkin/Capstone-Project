package net.catsonmars.android.stillinmemphis.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by pmatushkin on 4/26/2016.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final long PACKAGE_QUERY = 1L;

    // content://net.catsonmars.android.stillinmemphis/packages
    private static final Uri TEST_PACKAGES_DIR = TrackingContract.PackagesEntry.CONTENT_URI;

    // content://net.catsonmars.android.stillinmemphis/packages/#/*
    private static final Uri TEST_PACKAGE_WITH_EVENTS_ITEM = TrackingContract.PackagesEntry.buildPackageWithEventsUri(PACKAGE_QUERY);

    // content://net.catsonmars.android.stillinmemphis/packages/*/0
    private static final Uri TEST_PACKAGES_WITH_LATEST_EVENT_DIR = TrackingContract.PackagesEntry.buildPackagesWithLatestEventUri();

    // content://net.catsonmars.android.stillinmemphis/events
    private static final Uri TEST_EVENTS_DIR = TrackingContract.EventsEntry.CONTENT_URI;

    public void testUriMatcher() {
        UriMatcher testMatcher = TrackingProvider.buildUriMatcher();

        assertEquals("Error: The PACKAGES URI was matched incorrectly.",
                testMatcher.match(TEST_PACKAGES_DIR), TrackingProvider.PACKAGES);

        assertEquals("Error: The PACKAGE WITH EVENTS URI was matched incorrectly.",
                testMatcher.match(TEST_PACKAGE_WITH_EVENTS_ITEM), TrackingProvider.PACKAGE_WITH_EVENTS);

        assertEquals("Error: The PACKAGES WITH LATEST EVENT URI was matched incorrectly.",
                testMatcher.match(TEST_PACKAGES_WITH_LATEST_EVENT_DIR), TrackingProvider.PACKAGES_WITH_LATEST_EVENT);

        assertEquals("Error: The EVENTS URI was matched incorrectly.",
                testMatcher.match(TEST_EVENTS_DIR), TrackingProvider.EVENTS);
    }
}
