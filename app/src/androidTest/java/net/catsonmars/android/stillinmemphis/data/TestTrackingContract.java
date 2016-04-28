package net.catsonmars.android.stillinmemphis.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by pmatushkin on 4/27/2016.
 */
public class TestTrackingContract extends AndroidTestCase {
    private final long TEST_PACKAGE_ID = 1;

    private final String EXPECTED_URI = "content://net.catsonmars.android.stillinmemphis/events/1";

    public void testbuildEventPackage() {
        Uri locationUri = TrackingContract.EventsEntry.buildEventPackage(TEST_PACKAGE_ID);

        assertEquals("Error: Package id not properly appended to the end of the Uri",
                String.valueOf(TEST_PACKAGE_ID), locationUri.getLastPathSegment());
        assertEquals("Error: Events Uri doesn't match our expected result",
                locationUri.toString(),
                EXPECTED_URI);

    }
}
