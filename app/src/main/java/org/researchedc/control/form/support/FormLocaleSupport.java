package org.researchedc.control.form.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

public final class FormLocaleSupport {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private static final List<String> REQUIRED_BUNDLES = List.of(
        "org.researchedc.i18n.admin",
        "org.researchedc.i18n.audit_events",
        "org.researchedc.i18n.exceptions",
        "org.researchedc.i18n.format",
        "org.researchedc.i18n.page_messages",
        "org.researchedc.i18n.terms",
        "org.researchedc.i18n.notes",
        "org.researchedc.i18n.words",
        "org.researchedc.i18n.workflow"
    );

    private FormLocaleSupport() {
    }

    public static Locale getLocale(HttpServletRequest request) {
        Locale sessionLocale = getLocaleInSession(request == null ? null : request.getSession(false));
        if (sessionLocale != null) {
            return sessionLocale;
        }
        return resolveLocale(request);
    }

    public static Locale resolveLocale(HttpServletRequest request) {
        if (request != null) {
            for (Enumeration<Locale> locales = request.getLocales(); locales.hasMoreElements(); ) {
                Locale locale = locales.nextElement();
                if (isQualifiedLocale(locale)) {
                    return ResourceBundle.getBundle("org.researchedc.i18n.format", locale).getLocale();
                }
                if (DEFAULT_LOCALE.getLanguage().equalsIgnoreCase(locale.getLanguage())) {
                    break;
                }
            }
        }
        return DEFAULT_LOCALE;
    }

    public static Locale getDefaultLocale() {
        return DEFAULT_LOCALE;
    }

    private static Locale getLocaleInSession(HttpSession session) {
        if (session != null) {
            return (Locale) session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
        }
        return null;
    }

    private static boolean isQualifiedLocale(Locale locale) {
        for (String baseName : REQUIRED_BUNDLES) {
            ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
            Locale bundleLocale = bundle.getLocale();
            if (bundleLocale == null || bundleLocale.toString().isEmpty()
                || !bundleLocale.getLanguage().equals(locale.getLanguage())) {
                return false;
            }
        }
        return true;
    }
}
