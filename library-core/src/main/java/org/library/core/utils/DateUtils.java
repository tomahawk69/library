package org.library.core.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final String DB_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String localDateTimeToString(LocalDateTime localDateTime) {
        return localDateTime.format(getDBDateTimeFormatter());
    }

    public static LocalDateTime stringToLocalDateTime(String string) {
        return LocalDateTime.parse(string, getDBDateTimeFormatter());
    }

    public static DateTimeFormatter getDBDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(DB_DATETIME_PATTERN);
    }

}
