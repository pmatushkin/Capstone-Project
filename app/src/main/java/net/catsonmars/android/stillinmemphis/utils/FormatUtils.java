package net.catsonmars.android.stillinmemphis.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by pmatushkin on 5/17/2016.
 */
public class FormatUtils {

    public static String formatUSPSDateTime(String uspsDateString, String uspsTimeString) {
        String memphisDateString = formatUSPSDate(uspsDateString);
        String memphisTimeString = formatUSPSTime(uspsTimeString);

        if ("".equals(memphisDateString)) {
            return memphisTimeString;
        } else {
            if ("".equals(memphisTimeString)) {
                return memphisDateString;
            } else {
                return String.format("%s, %s", memphisDateString, memphisTimeString);
            }
        }
    }

    private static String formatUSPSDate(String uspsDateString) {
        String memphisDateString = "";

        SimpleDateFormat uspsDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        SimpleDateFormat memphisDateFormat = new SimpleDateFormat("MMM dd", Locale.US);

        if (uspsDateString != null
                && !"".equals(uspsDateString)) {
            try {
                memphisDateString = memphisDateFormat.format(uspsDateFormat.parse(uspsDateString));
            } catch (Exception e) {
                memphisDateString = uspsDateString;
            }
        }

        return null == memphisDateString ? "" : memphisDateString;
    }

    private static String formatUSPSTime(String uspsTimeString) {
        return null == uspsTimeString ? "" : uspsTimeString;
    }
}
