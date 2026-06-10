package com.pharmastock.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DateUtil() {
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    public static long daysUntil(LocalDate date) {
        if (date == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    public static boolean isExpired(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    public static boolean isNearExpiry(LocalDate date, int days) {
        if (date == null) return false;
        long daysDiff = daysUntil(date);
        return daysDiff >= 0 && daysDiff <= days;
    }
}
