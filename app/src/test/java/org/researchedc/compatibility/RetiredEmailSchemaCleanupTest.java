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

    @Test
    void oc20FoundationRetainsDeprecatedFacilityContactEmail() throws IOException {
        String xsd = loadResource("properties/OpenClinica-ODM1-3-0-OC2-0-foundation.xsd");

        assertTrue(xsd.contains("FacilityContactEmail"),
                "OC2-0 compatibility schema must retain FacilityContactEmail for downstream validators");
        assertTrue(xsd.contains("DEPRECATED"),
                "FacilityContactEmail must carry DEPRECATED annotation in OC2-0");
        assertTrue(xsd.contains("will be removed in a future version"),
                "FacilityContactEmail must state removal intent in OC2-0");
        assertTrue(xsd.contains("write-neutralized"),
                "FacilityContactEmail must document trigger-based neutralization in OC2-0");
    }

    @Test
    void oc21FoundationRemovesFacilityContactEmail() throws IOException {
        String xsd = loadResource("properties/OpenClinica-ODM1-3-0-OC2-1-foundation.xsd");

        assertFalse(xsd.contains("FacilityContactEmail"),
                "OC2-1 email-free schema must not contain FacilityContactEmail");
        assertTrue(xsd.contains("FacilityContactName"),
                "OC2-1 must retain FacilityContactName in FacilityInformation");
        assertTrue(xsd.contains("FacilityContactDegree"),
                "OC2-1 must retain FacilityContactDegree in FacilityInformation");
        assertTrue(xsd.contains("FacilityContactPhone"),
                "OC2-1 must retain FacilityContactPhone in FacilityInformation");
    }

    @Test
    void oc21FoundationFileExistsOnClasspath() throws IOException {
        String xsd = loadResource("properties/OpenClinica-ODM1-3-0-OC2-1-foundation.xsd");
        assertNotNull(xsd, "OC2-1 foundation XSD must be on classpath");

        String mainXsd = loadResource("properties/OpenClinica-ODM1-3-0-OC2-1.xsd");
        assertNotNull(mainXsd, "OC2-1 main XSD must be on classpath");

        String toOdmXsd = loadResource("properties/OpenClinica-ToODM1-3-0-OC2-1.xsd");
        assertNotNull(toOdmXsd, "OC2-1 ToODM XSD must be on classpath");
    }

    @Test
    void odmContractVersionResolvesToCorrectSchemaFiles() throws IOException {
        var resolver = new org.researchedc.module.export.service.OdmSchemaResourceResolver();

        var compatPaths = resolver.resolve(org.researchedc.module.export.enums.OdmContractVersion.OC2_0_COMPAT);
        assertNotNull(loadResource(compatPaths.mainXsd()), "OC2-0 main XSD must resolve");
        assertNotNull(loadResource(compatPaths.toOdmXsd()), "OC2-0 ToODM XSD must resolve");
        assertNotNull(loadResource(compatPaths.foundationXsd()), "OC2-0 foundation XSD must resolve");

        var newPaths = resolver.resolve(org.researchedc.module.export.enums.OdmContractVersion.OC2_1);
        assertNotNull(loadResource(newPaths.mainXsd()), "OC2-1 main XSD must resolve");
        assertNotNull(loadResource(newPaths.toOdmXsd()), "OC2-1 ToODM XSD must resolve");
        assertNotNull(loadResource(newPaths.foundationXsd()), "OC2-1 foundation XSD must resolve");
    }

    private String loadResource(String resourcePath) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(stream, "Missing resource: " + resourcePath);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
