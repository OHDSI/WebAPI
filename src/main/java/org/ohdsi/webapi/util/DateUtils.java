package org.ohdsi.webapi.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String dateToString(Date date) {
        if (date == null) return null;
        DateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
        return df.format(date);
    }
}
