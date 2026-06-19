package org.researchedc.module.dataimport.internal.support;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.core.io.ClassPathResource;

public final class ImportCompatibilitySupport {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final String PAGE_MESSAGES_BUNDLE = "org.researchedc.i18n.page_messages";
    private static final String ODM_MAPPING_RESOURCE = "properties/cd_odm_mapping.xml";

    private ImportCompatibilitySupport() {
    }

    public static String odmMappingLocation() throws IOException {
        return new ClassPathResource(ODM_MAPPING_RESOURCE).getURL().toExternalForm();
    }

    public static ResourceBundle pageMessagesBundle(Locale locale) {
        Locale resolvedLocale = locale == null ? DEFAULT_LOCALE : locale;
        return ResourceBundle.getBundle(PAGE_MESSAGES_BUNDLE, resolvedLocale);
    }
}
