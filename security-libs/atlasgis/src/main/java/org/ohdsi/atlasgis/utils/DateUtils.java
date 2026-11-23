package org.ohdsi.atlasgis.utils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.Optional;

public class DateUtils {

    private static final FastDateFormat formatter = DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT;

    public static String formatISO(Date date) {

        return formatISO(date, "");
    }

    public static String formatISO(Date date, String defaultValue) {

        return Optional.ofNullable(date).map(formatter::format).orElse(defaultValue);
    }
}
