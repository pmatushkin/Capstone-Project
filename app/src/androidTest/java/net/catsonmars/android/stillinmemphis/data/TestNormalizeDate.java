package net.catsonmars.android.stillinmemphis.data;

import android.test.AndroidTestCase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by pmatushkin on 4/26/2016.
 */
public class TestNormalizeDate extends AndroidTestCase {

    public void testNormalizeDate() {
        String dateValue = "February 23, 2016";
        String timeValue = "11:32 AM";

        // both dateValue and timeValue are present
        long normalizedDate = TrackingContract.normalizeDate(dateValue, timeValue);

        Date date = new Date(normalizedDate);
        SimpleDateFormat sdf = new SimpleDateFormat(TrackingContract.FORMAT_DATE_TIME, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals("Error: normalizeDate() failed to normalize date.",
                sdf.format(date), dateValue + " " + timeValue);

        // dateValue is present, timeValue is missing
        normalizedDate = TrackingContract.normalizeDate(dateValue, "");

        date = new Date(normalizedDate);

        assertEquals("Error: normalizeDate() failed to normalize date.",
                sdf.format(date), dateValue + " 12:00 AM");

        // dateValue is missing, timeValue is present
        normalizedDate = TrackingContract.normalizeDate("", timeValue);

        date = new Date(normalizedDate);

        Date todayDate = new Date();
        SimpleDateFormat sdfDate = new SimpleDateFormat(TrackingContract.FORMAT_DATE, Locale.US);
        sdfDate.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals("Error: normalizeDate() failed to normalize date.",
                sdf.format(date), sdfDate.format(todayDate) + " " + timeValue);
    }
}
