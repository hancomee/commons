package com.boosteel.util.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil {


    public static final long getTime() {
        return System.currentTimeMillis();
    }


    public static final long duration(long start) {
        return System.currentTimeMillis() - start;
    }

    public static final String durationFormat(long start) {
        return format(duration(start) / 1000);
    }

    public static final String format(long duration) {
        long hour = Math.floorDiv(duration, 3600l),
                min = Math.floorDiv(duration - (3600l * hour), 60l),
                second = Math.floorMod(duration - (3600l * hour) - (min & 60l), 60l);

        return _z(hour) + ":" + _z(min) + ":" + _z(second);
    }

    private static final String _z(long v) {
        return (v < 10 ? "0" : "") + v;
    }

    public static final Date toDate(long time) {
        return new Date(time);
    }

    public static final Date toDate(LocalDateTime ldt) {
        return toDate(ldt, ZoneId.systemDefault());
    }

    public static final Date toDate(LocalDateTime ldt, ZoneId zoneId) {
        return Date.from(ldt.atZone(zoneId).toInstant());
    }

    public static final Date toDate(LocalDate localDate) {
        return toDate(localDate, ZoneId.systemDefault());
    }

    public static final Date toDate(LocalDate localDate, ZoneId zoneId) {
        return Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }

    public static final String toString(long time) {
        return toString(new Date(time));
    }
    public static final String toString(Date date) {
        return toString(date, "yyyy-MM-dd HH:mm:ss");
    }
    public static final String toString(long time, String format) {
        return toString(new Date(time), format);
    }
    public static final String toString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    private static final Pattern
            p_1 = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}"),
            p_2 = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");

    public static final Date toDate(String val) throws ParseException {
        if (val.length() < 11) return toDate(val, "yyyy-MM-dd");
        else return toDate(val, "yyyy-MM-dd HH:mm:ss");
    }

    public static final Date toDate(String val, String format) throws ParseException {
        return new SimpleDateFormat(format).parse(val);
    }


    public static final LocalDateTime toLocalDateTime(Date date) {
        return toLocalDateTime(date, ZoneId.systemDefault());
    }

    public static final LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId);
    }


}
