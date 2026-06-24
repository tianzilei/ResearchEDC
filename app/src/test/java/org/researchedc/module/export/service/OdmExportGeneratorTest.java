package org.researchedc.module.export.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.researchedc.module.export.enums.OdmContractVersion;
import org.researchedc.module.export.internal.OdmFormDataSnapshot;
import org.researchedc.module.export.internal.OdmItemDataSnapshot;
import org.researchedc.module.export.internal.OdmItemGroupDataSnapshot;
import org.researchedc.module.export.internal.OdmStudyEventDataSnapshot;
import org.researchedc.module.export.internal.OdmStudySnapshot;
import org.researchedc.module.export.internal.OdmSubjectDataSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OdmExportGeneratorTest {

    private OdmExportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new OdmExportGenerator(new OdmSchemaResourceResolver());
    }

    @Test
    void generate_oc21_omitsFacilityContactEmail() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Test Study", "Description", "Protocol1",
                "FacilityName", "facility@example.com",
                "mdv_1_1", "Test MetaData");

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "file_oid_1");

        assertTrue(xml.contains("ODM"));
        assertTrue(xml.contains("StudyOID=\"S_1\""));
        assertFalse(xml.contains("FacilityContactEmail"),
                "OC2_1 should NOT contain FacilityContactEmail");
        assertFalse(xml.contains("StudyDetails"),
                "OC2_1 should NOT contain StudyDetails");
    }

    @Test
    void generate_oc20compat_includesFacilityContactEmail() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Test Study", "Description", "Protocol1",
                "MyFacility", "contact@example.com",
                "mdv_1_1", "Test MetaData");

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_0_COMPAT, "file_oid_2");

        assertTrue(xml.contains("FacilityContactEmail"));
        assertTrue(xml.contains("oc:StudyDetails") || xml.contains("StudyDetails"),
                "OC2_0_COMPAT should contain StudyDetails element");
        assertTrue(xml.contains("MyFacility"));
    }

    @Test
    void generate_withSubjectData_includesClinicalData() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        OdmItemDataSnapshot item = new OdmItemDataSnapshot("I_1", "value1", false);
        OdmItemGroupDataSnapshot group = new OdmItemGroupDataSnapshot("IG_1", null, List.of(item));
        OdmFormDataSnapshot form = new OdmFormDataSnapshot("F_1", null, List.of(group));
        OdmStudyEventDataSnapshot event = new OdmStudyEventDataSnapshot("SE_1", null, List.of(form));
        OdmSubjectDataSnapshot subject = new OdmSubjectDataSnapshot("SS_1", "label1", List.of(event));

        String xml = generator.generate(study, List.of(subject),
                OdmContractVersion.OC2_1, "file_oid_3");

        assertTrue(xml.contains("ClinicalData"));
        assertTrue(xml.contains("SubjectData"));
        assertTrue(xml.contains("SubjectKey=\"SS_1\""));
        assertTrue(xml.contains("StudyEventOID=\"SE_1\""));
        assertTrue(xml.contains("FormOID=\"F_1\""));
        assertTrue(xml.contains("ItemOID=\"I_1\""));
        assertTrue(xml.contains("value1"));
    }

    @Test
    void generate_emptySubjectList_producesEmptyClinicalData() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "file_oid_4");

        assertTrue(xml.contains("ClinicalData"));
        assertFalse(xml.contains("SubjectData"));
    }

    @Test
    void generate_schemaLocation_containsOdmNamespace() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "file_oid_5");

        assertTrue(xml.contains("http://www.cdisc.org/ns/odm/v1.3"));
    }

    @Test
    void generate_monitoredItem_includesMonitoredAttribute() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        OdmItemDataSnapshot item = new OdmItemDataSnapshot("I_1", "val", true);
        OdmItemGroupDataSnapshot group = new OdmItemGroupDataSnapshot("IG_1", null, List.of(item));
        OdmFormDataSnapshot form = new OdmFormDataSnapshot("F_1", null, List.of(group));
        OdmStudyEventDataSnapshot event = new OdmStudyEventDataSnapshot("SE_1", null, List.of(form));
        OdmSubjectDataSnapshot subject = new OdmSubjectDataSnapshot("SS_1", null, List.of(event));

        String xml = generator.generate(study, List.of(subject),
                OdmContractVersion.OC2_1, "file_oid_6");

        assertTrue(xml.contains("Monitored=\"Yes\""),
                "Should contain Monitored attribute with value Yes");
    }
}
