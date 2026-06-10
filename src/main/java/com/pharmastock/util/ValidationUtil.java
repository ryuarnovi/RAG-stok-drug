package com.pharmastock.util;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9\\-()\\s]{8,20}$");

    private ValidationUtil() {
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isPositiveNumber(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            return Double.parseDouble(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDate(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            java.time.LocalDate.parse(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
