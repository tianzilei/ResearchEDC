package org.researchedc.control.form.support;

import java.util.Locale;
import java.util.ResourceBundle;

public final class FormResourceBundleSupport {

    private FormResourceBundleSupport() {
    }

    public static ResourceBundle getFormatBundle(Locale locale) {
        return getBundle("org.researchedc.i18n.format", locale);
    }

    public static ResourceBundle getExceptionsBundle(Locale locale) {
        return getBundle("org.researchedc.i18n.exceptions", locale);
    }

    public static ResourceBundle getTextsBundle(Locale locale) {
        return getBundle("org.researchedc.i18n.notes", locale);
    }

    public static ResourceBundle getWordsBundle(Locale locale) {
        return getBundle("org.researchedc.i18n.words", locale);
    }

    private static ResourceBundle getBundle(String baseName, Locale locale) {
        Locale resolvedLocale = locale == null ? FormLocaleSupport.getDefaultLocale() : locale;
        return ResourceBundle.getBundle(baseName, resolvedLocale);
    }
}
