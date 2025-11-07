package com.hionstudios.time;

import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

public interface TimeUtil {
    TimeZone TIMEZONE = TimeZone.getTimeZone("Asia/Kolkata");

    static long start(long time) {
        Calendar cal = new ISTCalender();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTimeInMillis();
    }

    static long currentTime() {
        return System.currentTimeMillis();
    }

    static String toString(long date) {
        return toString(date, "dd-MM-yyyy HH:mm:ss a");
    }

    static String toString(long date, String format) {
        return new ISTDateFormat(format).format(date);
    }

    static String toString(String format) {
        return toString(currentTime(), format);
    }

    static long getTime() {
        return currentTime();
    }

    static String toDateString(long date) {
        return toString(date, "dd-MM-yyyy");
    }

    static String toDateString() {
        return toDateString(currentTime());
    }

    static String toTimeString(long date) {
        return toString(date, "HH:mm:ss");
    }

    static int getCurrentYear() {
        return Integer.parseInt(toString(getTime(), "yyyy"));
    }

    static long today() {
        return start(getTime());
    }

    static long parse(String time, String format) {
        return new ISTDateFormat(format).parse(time).getTime();
    }

    static long parseDate(String date) {
        return parse(date, "dd-MM-yyyy");
    }

    // Combine a date (epoch millis for any time that day) and millisSinceMidnight
    public static String toRFC3339FromDateAndTime(long dateEpoch, long millisOfDay) {
        // Normalize to midnight (00:00 of that day)
        java.time.ZonedDateTime midnight = Instant.ofEpochMilli(dateEpoch)
                .atZone(TIMEZONE.toZoneId())
                .toLocalDate()
                .atStartOfDay(TIMEZONE.toZoneId());

        // Add the time offset of the day
        java.time.ZonedDateTime zonedDateTime = midnight.plusNanos(millisOfDay * 1_000_000L);

        // Return proper RFC3339 formatted timestamp
        return zonedDateTime.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    static long normalizeToMidnight(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis)
                .atZone(TIMEZONE.toZoneId())
                .toLocalDate()
                .atStartOfDay(TIMEZONE.toZoneId())
                .toInstant()
                .toEpochMilli();
    }

}
