package org.researchedc.compatibility;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class RetiredEmailSchemaCleanupTest {

    @Test
    void migrationRetiresEmailStorageAndKeepsCompatibilityWritesInert() throws IOException {
        String releaseXml = loadResource("migration/3.18/release.xml");
        String migrationXml = loadResource("migration/3.18/2026-06-21-retire-email-storage.xml");

        assertTrue(releaseXml.contains("migration/3.18/2026-06-21-retire-email-storage.xml"));

        assertTrue(migrationXml.contains("dropTable tableName=\"dc_send_email_event\""));
        assertTrue(migrationXml.contains("dropColumn tableName=\"rule_action\" columnName=\"email_to\""));
        assertTrue(migrationXml.contains("dropColumn tableName=\"rule_action\" columnName=\"email_subject\""));

        assertTrue(migrationXml.contains("trg_neutralize_user_account_email"));
        assertTrue(migrationXml.contains("trg_neutralize_module_user_account_email"));
        assertTrue(migrationXml.contains("trg_neutralize_study_contact_email"));
        assertTrue(migrationXml.contains("trg_neutralize_module_study_contact_email"));
    }

    @Test
    void ruleXsdNoLongerExposesRetiredEmailActionContract() throws IOException {
        String rulesXsd = loadResource("properties/rules-ODM.xsd");

        assertFalse(rulesXsd.contains("EmailAction"));
        assertTrue(rulesXsd.contains("DiscrepancyNoteAction"));
        assertTrue(rulesXsd.contains("NotificationAction"));
    }

    private String loadResource(String resourcePath) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(stream, "Missing resource: " + resourcePath);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
