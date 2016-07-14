package org.library.core.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String localDateTimeToString(LocalDateTime localDateTime) {
        return localDateTime.format(formatter);
    }

    public static LocalDateTime stringToLocalDateTime(String string) {
        return LocalDateTime.parse(string, formatter);
    }

}
