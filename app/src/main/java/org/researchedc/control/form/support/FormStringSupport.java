package org.researchedc.control.form.support;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class FormStringSupport {

    private FormStringSupport() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().equals("");
    }

    public static boolean isFormatDate(String value, String dateFormat, Locale locale) {
        String normalized = normalizePattern(dateFormat);
        Locale resolvedLocale = resolveLocale(locale);
        SimpleDateFormat parser = new SimpleDateFormat(normalized, resolvedLocale);
        parser.setLenient(false);
        SimpleDateFormat formatter = new SimpleDateFormat(normalized, resolvedLocale);
        formatter.setLenient(false);
        try {
            Date parsed = parser.parse(value);
            String reformatted = formatter.format(parsed);
            if (!reformatted.equals(value)) {
                return false;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsed);
            int year = calendar.get(Calendar.YEAR);
            return year <= 9999 && year >= 1000;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDateFormatString(String value, String dateFormat, Locale locale) {
        SimpleDateFormat format = new SimpleDateFormat(normalizePattern(dateFormat), resolveLocale(locale));
        format.setLenient(false);
        try {
            format.parse(value);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isPartialYear(String value, String yearFormat, Locale locale) {
        return partialYear(value, yearFormat, locale);
    }

    public static boolean isPartialYearMonth(String value, String yearMonthFormat, Locale locale) {
        String normalized = normalizePattern(yearMonthFormat) + "-dd";
        String synthetic = value + "-18";
        Locale resolvedLocale = resolveLocale(locale);
        SimpleDateFormat parser = new SimpleDateFormat(normalized, resolvedLocale);
        parser.setLenient(false);
        SimpleDateFormat formatter = new SimpleDateFormat(normalized, resolvedLocale);
        formatter.setLenient(false);
        try {
            Date parsed = parser.parse(synthetic);
            String reformatted = formatter.format(parsed);
            if (!reformatted.equals(synthetic)) {
                return false;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsed);
            int year = calendar.get(Calendar.YEAR);
            return year <= 9999 && year >= 1000;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean partialYear(String value, String yearFormat, Locale locale) {
        int digits = 0;
        for (char c : value.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
            digits++;
        }
        if (digits != 4) {
            return false;
        }
        String normalized = normalizePattern(yearFormat) + "-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(normalized, resolveLocale(locale));
        format.setLenient(false);
        try {
            format.parse(value + "-01-18");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String normalizePattern(String pattern) {
        String normalized = pattern;
        while (normalized.contains("Y")) {
            normalized = normalized.replace("Y", "y");
        }
        while (normalized.contains("D")) {
            normalized = normalized.replace("D", "d");
        }
        return normalized;
    }

    private static Locale resolveLocale(Locale locale) {
        return locale == null ? FormLocaleSupport.getDefaultLocale() : locale;
    }
}
