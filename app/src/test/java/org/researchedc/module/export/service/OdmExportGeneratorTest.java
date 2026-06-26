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

    @Test
    void generate_rootAttributes_fileTypeAndOidAreSet() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "MY_FILE_OID");

        assertTrue(xml.contains("FileType=\"Snapshot\""),
                "Root should have FileType=Snapshot");
        assertTrue(xml.contains("FileOID=\"MY_FILE_OID\""),
                "Root should have the specified FileOID");
        assertTrue(xml.contains("ODMVersion=\"1.3.2\""),
                "Root should declare ODMVersion 1.3.2");
    }

    @Test
    void generate_rootAttributes_creationDateTimeIsPresent() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "file_oid_dt");

        assertTrue(xml.contains("CreationDateTime=\""),
                "Root should have CreationDateTime attribute");
    }

    @Test
    void generate_oc21_schemaLocation_hasBothOdmAndOcNamespaces() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "file_oid_sl");

        assertTrue(xml.contains("http://www.cdisc.org/ns/odm/v1.3"),
                "SchemaLocation should reference ODM namespace");
        assertTrue(xml.contains("http://www.openclinica.org/ns/odm_ext_v130/v3.1"),
                "SchemaLocation should reference OC namespace");
        assertTrue(xml.contains("OpenClinica-ODM1-3-0-OC2-1.xsd"),
                "SchemaLocation should reference OC2-1 main XSD");
        assertTrue(xml.contains("OpenClinica-ToODM1-3-0-OC2-1.xsd"),
                "SchemaLocation should reference OC2-1 ToODM XSD");
    }

    @Test
    void generate_oc20compat_schemaLocation_referencesOc20Xsd() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_0_COMPAT, "file_oid_sl20");

        assertTrue(xml.contains("OpenClinica-ODM1-3-0-OC2-0.xsd"),
                "OC2_0_COMPAT SchemaLocation should reference OC2-0 main XSD");
        assertTrue(xml.contains("OpenClinica-ToODM1-3-0-OC2-0.xsd"),
                "OC2_0_COMPAT SchemaLocation should reference OC2-0 ToODM XSD");
    }

    @Test
    void generate_namespaceDeclarations_ocAndXsiAreDeclared() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "file_oid_ns");

        assertTrue(xml.contains("xmlns:oc=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\""),
                "Should declare xmlns:oc namespace");
        assertTrue(xml.contains("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""),
                "Should declare xmlns:xsi namespace");
    }

    @Test
    void generate_studyElement_hasCorrectOidAndName() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "STUDY_ABC", "My Study", "A description", "Protocol X",
                null, null, "mdv_abc_1", "MetaData Version 1");

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "file_oid_study");

        assertTrue(xml.contains("OID=\"STUDY_ABC\""),
                "Study element should have correct OID");
        assertTrue(xml.contains("<StudyName>My Study</StudyName>"),
                "Study should contain StudyName");
        assertTrue(xml.contains("<StudyDescription>A description</StudyDescription>"),
                "Study should contain StudyDescription");
        assertTrue(xml.contains("<ProtocolName>Protocol X</ProtocolName>"),
                "Study should contain ProtocolName");
    }

    @Test
    void generate_studyElement_protocolNameDefaultsToStudyNameWhenNull() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Fallback Name", null, null,
                null, null, "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "file_oid_proto");

        assertTrue(xml.contains("<ProtocolName>Fallback Name</ProtocolName>"),
                "ProtocolName should default to StudyName when protocol is null");
    }

    @Test
    void generate_metaDataVersion_hasOidAndName() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "MDV_OID_123", "My MetaData");

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "file_oid_mdv");

        assertTrue(xml.contains("MetaDataVersionOID=\"MDV_OID_123\""),
                "MetaDataVersion should have correct OID");
        assertTrue(xml.contains("Name=\"My MetaData\""),
                "MetaDataVersion should have correct Name");
    }

    @Test
    void generate_clinicalData_hasStudyOidAndMetaDataVersionOid() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_1, "file_oid_cd");

        assertTrue(xml.contains("StudyOID=\"S_1\""),
                "ClinicalData should reference StudyOID");
        assertTrue(xml.contains("MetaDataVersionOID=\"mdv_1_1\""),
                "ClinicalData should reference MetaDataVersionOID");
    }

    @Test
    void generate_multipleSubjects_allSubjectsAreIncluded() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        OdmSubjectDataSnapshot subject1 = new OdmSubjectDataSnapshot("SS_1", null, Collections.emptyList());
        OdmSubjectDataSnapshot subject2 = new OdmSubjectDataSnapshot("SS_2", null, Collections.emptyList());
        OdmSubjectDataSnapshot subject3 = new OdmSubjectDataSnapshot("SS_3", null, Collections.emptyList());

        String xml = generator.generate(study, List.of(subject1, subject2, subject3),
                OdmContractVersion.OC2_1, "file_oid_multi");

        assertTrue(xml.contains("SubjectKey=\"SS_1\""));
        assertTrue(xml.contains("SubjectKey=\"SS_2\""));
        assertTrue(xml.contains("SubjectKey=\"SS_3\""));
    }

    @Test
    void generate_itemData_emptyValue_producesEmptyAttribute() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        OdmItemDataSnapshot item = new OdmItemDataSnapshot("I_1", null, false);
        OdmItemGroupDataSnapshot group = new OdmItemGroupDataSnapshot("IG_1", null, List.of(item));
        OdmFormDataSnapshot form = new OdmFormDataSnapshot("F_1", null, List.of(group));
        OdmStudyEventDataSnapshot event = new OdmStudyEventDataSnapshot("SE_1", null, List.of(form));
        OdmSubjectDataSnapshot subject = new OdmSubjectDataSnapshot("SS_1", null, List.of(event));

        String xml = generator.generate(study, List.of(subject),
                OdmContractVersion.OC2_1, "file_oid_empty");

        assertTrue(xml.contains("ItemOID=\"I_1\""),
                "Item should be present");
        assertTrue(xml.contains("Value=\"\""),
                "Null item value should produce empty Value attribute");
    }

    @Test
    void generate_studyEventData_repeatKey_isIncludedWhenPresent() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        OdmStudyEventDataSnapshot event = new OdmStudyEventDataSnapshot("SE_1", "repeat_42", Collections.emptyList());
        OdmSubjectDataSnapshot subject = new OdmSubjectDataSnapshot("SS_1", null, List.of(event));

        String xml = generator.generate(study, List.of(subject),
                OdmContractVersion.OC2_1, "file_oid_rk");

        assertTrue(xml.contains("EventRepeatKey=\"repeat_42\""),
                "StudyEventData should include EventRepeatKey when present");
    }

    @Test
    void generate_studyEventData_noRepeatKey_omitsAttribute() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null, null, null, "mdv_1_1", null);

        OdmStudyEventDataSnapshot event = new OdmStudyEventDataSnapshot("SE_1", null, Collections.emptyList());
        OdmSubjectDataSnapshot subject = new OdmSubjectDataSnapshot("SS_1", null, List.of(event));

        String xml = generator.generate(study, List.of(subject),
                OdmContractVersion.OC2_1, "file_oid_nork");

        assertFalse(xml.contains("EventRepeatKey"),
                "StudyEventData should NOT have EventRepeatKey when null");
    }

    @Test
    void generate_oc20compat_studyDetails_hasFacilityNameAndEmail() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null,
                "General Hospital", "admin@hospital.org",
                "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_0_COMPAT, "file_oid_sd");

        assertTrue(xml.contains("FacilityName"),
                "OC2_0_COMPAT should contain FacilityName element");
        assertTrue(xml.contains("General Hospital"),
                "FacilityName should have correct value");
        assertTrue(xml.contains("FacilityContactEmail"),
                "OC2_0_COMPAT should contain FacilityContactEmail element");
        assertTrue(xml.contains("admin@hospital.org"),
                "FacilityContactEmail should have correct value");
    }

    @Test
    void generate_oc20compat_emptyFacility_producesEmptyElements() {
        OdmStudySnapshot study = new OdmStudySnapshot(
                "S_1", "Study", null, null,
                null, null,
                "mdv_1_1", null);

        String xml = generator.generate(study, Collections.emptyList(),
                OdmContractVersion.OC2_0_COMPAT, "file_oid_ef");

        assertTrue(xml.contains("FacilityName"),
                "OC2_0_COMPAT should still contain FacilityName element");
        assertTrue(xml.contains("<FacilityName/>"),
                "Empty FacilityName should produce self-closing element");
        assertTrue(xml.contains("FacilityContactEmail"),
                "OC2_0_COMPAT should still contain FacilityContactEmail element");
        assertTrue(xml.contains("<FacilityContactEmail/>"),
                "Empty FacilityContactEmail should produce self-closing element");
    }
}
