package org.researchedc.control.form.support;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class FormFormatSupport {

    private FormFormatSupport() {
    }

    public static SimpleDateFormat getDateFormat(Locale locale) {
        Locale resolvedLocale = resolveLocale(locale);
        return new SimpleDateFormat(dateFormatString(resolvedLocale), resolvedLocale);
    }

    public static String dateFormatString(Locale locale) {
        return normalizePattern(FormResourceBundleSupport.getFormatBundle(resolveLocale(locale)).getString("date_format_string"));
    }

    public static String yearMonthFormatString(Locale locale) {
        return normalizePattern(FormResourceBundleSupport.getFormatBundle(resolveLocale(locale)).getString("date_format_year_month"));
    }

    private static Locale resolveLocale(Locale locale) {
        return locale == null ? FormLocaleSupport.getDefaultLocale() : locale;
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
}
