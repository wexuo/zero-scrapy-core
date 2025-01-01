package com.wexuo.scrapy.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil {

    private static final String DEFAULT_FORMATTER = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_PATTERN = "yyyy-MM-dd";

    public static final String TIME_PATTERN = "HH:mm:ss";

    public static final String DATE_MATCH_PATTERN = "\\d{4}-\\d{1,2}-\\d{1,2}";

    public static final String DATE_TIME_MATCH_PATTERN = "\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}";

    public static final String ISO_OFFSET_DATE_TIME_MATCH_PATTERN = "\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\+\\d{1,2}:\\d{1,2}";

    public static String now() {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_FORMATTER);
        return LocalDateTime.now().format(dateTimeFormatter);
    }

    public static String getLocalTimeStr() {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
        return LocalDateTime.now().format(dateTimeFormatter);
    }

    public static String format(final LocalDateTime time, final String format) {
        return time.format(DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime parseLocalDateTime(final String localDateTimeStr, final String pattern) {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(localDateTimeStr, dateTimeFormatter);
    }

    public static LocalDateTime of(final LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.now());
    }

    public static LocalDateTime of(final LocalDate localDate, final LocalTime localTime) {
        return LocalDateTime.of(localDate, localTime);
    }

    public static LocalDate parseLocalDate(final String localDateStr, final String pattern) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(localDateStr, formatter);
    }

    public static LocalDate from(final LocalDateTime localDateTime) {
        return LocalDate.of(localDateTime.getYear(), localDateTime.getMonth(), localDateTime.getDayOfMonth());
    }

    public static LocalDateTime plus(final LocalDateTime localDateTime, final int num, final ChronoUnit chronoUnit) {
        return localDateTime.plus(num, chronoUnit);
    }

    public static LocalDateTime minus(final LocalDateTime localDateTime, final int num, final ChronoUnit chronoUnit) {
        return localDateTime.minus(num, chronoUnit);
    }

    public static Boolean isDate(final String date) {
        final Pattern pattern = Pattern.compile(DATE_MATCH_PATTERN);
        final Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }

    public static Boolean isISOOffsetDateTime(final String datetime) {
        final Pattern pattern = Pattern.compile(ISO_OFFSET_DATE_TIME_MATCH_PATTERN);
        final Matcher matcher = pattern.matcher(datetime);
        return matcher.matches();
    }

    public static Boolean isDateTime(final String datetime) {
        final Pattern pattern = Pattern.compile(DATE_TIME_MATCH_PATTERN);
        final Matcher matcher = pattern.matcher(datetime);
        return matcher.matches();
    }

    public static Boolean isFormatDateTime(final String datetime) {
        return datetime.contains("分钟前") || datetime.contains("小时前") || datetime.contains("天前") || datetime.contains("年前");
    }

    public static Integer getDigit(final String str) {
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (Character.isDigit(c)) {
                buffer.append(c);
            }
        }
        return Integer.valueOf(buffer.toString());
    }

    public static String parseFormatDateTime(final String datetime) {
        if (Objects.isNull(datetime)) {
            return null;
        }
        if (isFormatDateTime(datetime)) {
            final LocalDateTime now = LocalDateTime.now();
            final int num = getDigit(datetime);
            LocalDateTime minus = now;
            if (datetime.contains("分钟前")) {
                minus = minus(now, num, ChronoUnit.MINUTES);
                return format(minus, DEFAULT_FORMATTER);
            } else if (datetime.contains("小时前")) {
                minus = minus(now, num, ChronoUnit.HOURS);
            } else if (datetime.contains("天前")) {
                minus = minus(now, num, ChronoUnit.DAYS);
            } else if (datetime.contains("年前")) {
                minus = minus(now, num, ChronoUnit.YEARS);
            }
            return format(minus, DEFAULT_FORMATTER);
        }
        if (isDate(datetime)) {
            final LocalDate localDate = parseLocalDate(datetime, DATE_PATTERN);
            return format(of(localDate), DEFAULT_FORMATTER);
        }
        if (isISOOffsetDateTime(datetime)) {
            final LocalDateTime localDateTime = LocalDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return format(localDateTime, DEFAULT_FORMATTER);
        }
        return datetime;
    }

    public static void main(final String[] args) {
        System.out.println(parseFormatDateTime("2天前"));
    }
}
