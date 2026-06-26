package org.researchedc.compatibility;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class RetiredEmailRuntimeContentTest {

    @Test
    void wordsBundles_doNotExposeRetiredEmailLabels() throws IOException {
        Properties english = loadProperties("org/researchedc/i18n/words.properties");
        Properties chinese = loadProperties("org/researchedc/i18n/words_zh.properties");

        assertFalse(english.containsKey("confirm_email"));
        assertFalse(english.containsKey("contact_email"));
        assertFalse(english.containsKey("your_email"));

        assertFalse(chinese.containsKey("confirm_email"));
        assertFalse(chinese.containsKey("contact_email"));
        assertFalse(chinese.containsKey("your_email"));
    }

    @Test
    void ruleNotes_doNotAdvertiseEmailActionsOrEnterpriseSupport() throws IOException {
        Properties english = loadProperties("org/researchedc/i18n/notes.properties");
        Properties chinese = loadProperties("org/researchedc/i18n/notes_zh.properties");

        assertFalse(english.getProperty("manage_execute_rule_assignments").contains("email action"));
        assertFalse(english.getProperty("technical_administration_network").contains("Enterprise"));
        assertTrue(english.getProperty("manage_execute_rule_assignments").contains("discrepancy actions"));

        assertFalse(chinese.getProperty("manage_execute_rule_assignments").contains("电子邮件操作"));
        assertFalse(chinese.getProperty("technical_administration_network").contains("Enterprise"));
        assertTrue(chinese.getProperty("manage_execute_rule_assignments").contains("差异备注操作"));
    }

    @Test
    void footerTooltips_useNeutralRetiredServiceWording() throws IOException {
        Properties words = loadProperties("org/researchedc/i18n/words.properties");
        Properties wordsZh = loadProperties("org/researchedc/i18n/words_zh.properties");
        Properties licensing = loadProperties("org/researchedc/i18n/licensing.properties");

        assertFalse(words.getProperty("footer.tooltip").contains("Enterprise"));
        assertFalse(wordsZh.getProperty("footer.tooltip").contains("Enterprise"));
        assertFalse(licensing.getProperty("footer.tooltip").contains("Enterprise"));

        assertTrue(words.getProperty("footer.tooltip").contains("mail delivery"));
        assertTrue(wordsZh.getProperty("footer.tooltip").contains("邮件发送"));
        assertTrue(licensing.getProperty("footer.tooltip").contains("mail delivery"));
    }

    private Properties loadProperties(String resourcePath) throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IOException("Missing resource: " + resourcePath);
            }
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
        return properties;
    }
}
