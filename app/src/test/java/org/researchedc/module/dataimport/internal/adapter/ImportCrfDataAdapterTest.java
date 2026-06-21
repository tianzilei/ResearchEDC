package org.researchedc.module.dataimport.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.submit.crfdata.CRFDataPostImportContainer;
import org.researchedc.bean.submit.crfdata.FormDataBean;
import org.researchedc.bean.submit.crfdata.ImportItemDataBean;
import org.researchedc.bean.submit.crfdata.ImportItemGroupDataBean;
import org.researchedc.bean.submit.crfdata.ODMContainer;
import org.researchedc.bean.submit.crfdata.StudyEventDataBean;
import org.researchedc.bean.submit.crfdata.SubjectDataBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.module.dataimport.service.ImportCrfVersionPort;
import org.researchedc.module.dataimport.service.ImportEventCrfPort;
import org.researchedc.module.dataimport.dto.ImportEventCrf;
import org.researchedc.module.dataimport.service.ImportItemDataPort;
import org.researchedc.module.dataimport.service.ImportItemFormMetadataPort;
import org.researchedc.module.dataimport.service.ImportItemGroupPort;
import org.researchedc.module.dataimport.service.ImportItemPort;
import org.researchedc.module.dataimport.service.ImportResponseSetPort;
import org.researchedc.module.dataimport.service.ImportStudyEventDefinitionPort;
import org.researchedc.module.dataimport.dto.ImportStudyEventDefinition;
import org.researchedc.module.dataimport.service.ImportStudyEventPort;
import org.researchedc.module.dataimport.dto.ImportStudyEvent;
import org.researchedc.module.dataimport.service.ImportStudyLookupPort;
import org.researchedc.module.dataimport.dto.ImportStudy;
import org.researchedc.module.dataimport.service.ImportStudySubjectPort;
import org.researchedc.module.dataimport.dto.ImportStudySubject;

class ImportCrfDataAdapterTest {

    @Test
    void parseOdm_readsRepresentativeClinicalDataFixture() throws Exception {
        Path fixture = Files.createTempFile("representative-crf-data", ".xml");
        Files.writeString(fixture, """
                <?xml version="1.0" encoding="UTF-8"?>
                <ODM xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1">
                  <ClinicalData StudyOID="S_DEMO">
                    <SubjectData SubjectKey="SS_DEMO">
                      <StudyEventData StudyEventOID="SE_BASELINE" StudyEventRepeatKey="1">
                        <FormData FormOID="F_VITALS_V1" OpenClinica:Status="data entry started">
                          <ItemGroupData ItemGroupOID="IG_VITALS" ItemGroupRepeatKey="1">
                            <ItemData ItemOID="I_WEIGHT" Value="70" TransactionType="Insert"/>
                            <ItemData ItemOID="I_VISIT_DATE" Value="2026-06-13" TransactionType="Insert"/>
                          </ItemGroupData>
                        </FormData>
                      </StudyEventData>
                    </SubjectData>
                  </ClinicalData>
                </ODM>
                """);

        try {
            ImportCrfDataAdapter adapter = new ImportCrfDataAdapter(
                    mock(ImportItemDataPort.class),
                    mock(ImportItemPort.class),
                    mock(ImportItemGroupPort.class),
                    mock(ImportItemFormMetadataPort.class),
                    mock(ImportStudyLookupPort.class),
                    mock(ImportStudySubjectPort.class),
                    mock(ImportStudyEventPort.class),
                    mock(ImportStudyEventDefinitionPort.class),
                    mock(ImportCrfVersionPort.class),
                    mock(ImportEventCrfPort.class),
                    mock(ImportResponseSetPort.class));

            ODMContainer odm = adapter.parseOdm(fixture).odm();

            assertNotNull(odm.getCrfDataPostImportContainer());
            assertEquals("S_DEMO", odm.getCrfDataPostImportContainer().getStudyOID());
            SubjectDataBean subject = odm.getCrfDataPostImportContainer().getSubjectData().get(0);
            assertEquals("SS_DEMO", subject.getSubjectOID());
            StudyEventDataBean event = subject.getStudyEventData().get(0);
            assertEquals("SE_BASELINE", event.getStudyEventOID());
            assertEquals("1", event.getStudyEventRepeatKey());
            FormDataBean form = event.getFormData().get(0);
            assertEquals("F_VITALS_V1", form.getFormOID());
            assertEquals("data entry started", form.getEventCRFStatus());
            ImportItemGroupDataBean group = form.getItemGroupData().get(0);
            assertEquals("IG_VITALS", group.getItemGroupOID());
            assertEquals("1", group.getItemGroupRepeatKey());
            assertEquals("I_WEIGHT", group.getItemData().get(0).getItemOID());
            assertEquals("70", group.getItemData().get(0).getValue());
            assertEquals("I_VISIT_DATE", group.getItemData().get(1).getItemOID());
            assertEquals("2026-06-13", group.getItemData().get(1).getValue());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    @Test
    void commitImport_whenItemPersistenceFails_throwsToTriggerTransactionRollback() {
        ImportItemDataPort itemDataPort = mock(ImportItemDataPort.class);
        ImportItemPort itemPort = mock(ImportItemPort.class);
        ImportStudyLookupPort studyLookupPort = mock(ImportStudyLookupPort.class);
        ImportStudySubjectPort studySubjectPort = mock(ImportStudySubjectPort.class);
        ImportStudyEventPort studyEventPort = mock(ImportStudyEventPort.class);
        ImportStudyEventDefinitionPort studyEventDefinitionPort = mock(ImportStudyEventDefinitionPort.class);
        ImportCrfVersionPort crfVersionPort = mock(ImportCrfVersionPort.class);
        ImportEventCrfPort eventCrfPort = mock(ImportEventCrfPort.class);
        ImportCrfDataAdapter adapter = new ImportCrfDataAdapter(
                itemDataPort,
                itemPort,
                mock(ImportItemGroupPort.class),
                mock(ImportItemFormMetadataPort.class),
                studyLookupPort,
                studySubjectPort,
                studyEventPort,
                studyEventDefinitionPort,
                crfVersionPort,
                eventCrfPort,
                mock(ImportResponseSetPort.class));

        ODMContainer odm = odmWithOneItem("S_DEMO", "SS_DEMO", "SE_BASELINE", "F_VITALS_V1", "I_WEIGHT", "70");
        StudySubjectBean subject = new StudySubjectBean();
        subject.setId(2);
        subject.setName("SS_DEMO");
        StudyEventDefinitionBean definition = new StudyEventDefinitionBean();
        definition.setId(3);
        definition.setName("Baseline");
        StudyEventBean event = new StudyEventBean();
        event.setId(4);
        event.setSubjectEventStatus(SubjectEventStatus.SCHEDULED);
        CRFVersionBean version = new CRFVersionBean();
        version.setId(5);

        when(studyLookupPort.findImportStudyByOid("S_DEMO"))
                .thenReturn(new ImportStudy(1, 0, "Demo Study"));
        when(studySubjectPort.findImportStudySubjectByOidAndStudy("SS_DEMO", 1))
                .thenReturn(new ImportStudySubject(2, "SS_DEMO"));
        when(studyEventDefinitionPort.findImportStudyEventDefinitionByOidAndStudy("SE_BASELINE", 1, 0))
                .thenReturn(new ImportStudyEventDefinition(3, "Baseline"));
        when(studyEventPort.findImportStudyEventBySubjectDefinitionOrdinal(2, 3, 1))
                .thenReturn(new ImportStudyEvent(4, SubjectEventStatus.SCHEDULED.getId(), ""));
        when(crfVersionPort.findAllImportCrfVersionsByOid("F_VITALS_V1"))
                .thenReturn(List.of(new ImportCrfVersionPort.ImportCrfVersion(5)));
        when(eventCrfPort.findImportEventCrfsByEventSubjectVersion(4, 2, 5))
                .thenReturn(new ArrayList<>());
        when(eventCrfPort.findImportEventCrfsByEventSubjectCrfId(4, 2, 5))
                .thenReturn(new ArrayList<>());
        when(eventCrfPort.createImportEventCrf(4, 2, 5, 0, "system", 1))
                .thenReturn(new ImportEventCrf(12, 5, 1));
        when(itemPort.findImportItemsByOid("I_WEIGHT"))
                .thenReturn(List.of(new ImportItemPort.ImportItem(77, "I_WEIGHT", 7)));
        org.mockito.Mockito.doThrow(new RuntimeException("database write failed"))
                .when(itemDataPort)
                .upsertImportItemData(
                        any(Integer.class),
                        any(Integer.class),
                        any(Integer.class),
                        any(Integer.class),
                        any(Integer.class),
                        any(String.class));

        assertThrows(IllegalStateException.class,
                () -> adapter.commitImport(new ImportCrfDataAdapter.ParsedOdm(odm), 1, Locale.ENGLISH));
        verify(itemDataPort).upsertImportItemData(
                any(Integer.class),
                any(Integer.class),
                any(Integer.class),
                any(Integer.class),
                any(Integer.class),
                any(String.class));
    }

    @Test
    void validateMetadata_whenStudyMissing_returnsLegacyErrorWithoutCallingLegacyService() {
        ImportStudyLookupPort studyLookupPort = mock(ImportStudyLookupPort.class);
        when(studyLookupPort.findImportStudyByOid("S_MISSING")).thenReturn(null);

        ImportCrfDataAdapter adapter = new ImportCrfDataAdapter(
                mock(ImportItemDataPort.class),
                mock(ImportItemPort.class),
                mock(ImportItemGroupPort.class),
                mock(ImportItemFormMetadataPort.class),
                studyLookupPort,
                mock(ImportStudySubjectPort.class),
                mock(ImportStudyEventPort.class),
                mock(ImportStudyEventDefinitionPort.class),
                mock(ImportCrfVersionPort.class),
                mock(ImportEventCrfPort.class),
                mock(ImportResponseSetPort.class));

        ODMContainer odm = new ODMContainer();
        CRFDataPostImportContainer postImport = new CRFDataPostImportContainer();
        postImport.setStudyOID("S_MISSING");
        odm.setCrfDataPostImportContainer(postImport);

        List<String> errors = adapter.validateMetadata(
                new ImportCrfDataAdapter.ParsedOdm(odm), 1, Locale.ENGLISH);

        assertEquals(1, errors.size());
        verify(studyLookupPort).findImportStudyByOid("S_MISSING");
    }

    private ODMContainer odmWithOneItem(
            String studyOid,
            String subjectOid,
            String studyEventOid,
            String formOid,
            String itemOid,
            String value) {
        ImportItemDataBean item = new ImportItemDataBean();
        item.setItemOID(itemOid);
        item.setValue(value);

        ImportItemGroupDataBean group = new ImportItemGroupDataBean();
        group.setItemGroupOID("IG_VITALS");
        group.setItemGroupRepeatKey("1");
        group.setItemData(new ArrayList<>(List.of(item)));

        FormDataBean form = new FormDataBean();
        form.setFormOID(formOid);
        form.setItemGroupData(new ArrayList<>(List.of(group)));

        StudyEventDataBean event = new StudyEventDataBean();
        event.setStudyEventOID(studyEventOid);
        event.setStudyEventRepeatKey("1");
        event.setFormData(new ArrayList<>(List.of(form)));

        SubjectDataBean subject = new SubjectDataBean();
        subject.setSubjectOID(subjectOid);
        subject.setStudyEventData(new ArrayList<>(List.of(event)));

        CRFDataPostImportContainer postImport = new CRFDataPostImportContainer();
        postImport.setStudyOID(studyOid);
        postImport.setSubjectData(new ArrayList<>(List.of(subject)));

        ODMContainer odm = new ODMContainer();
        odm.setCrfDataPostImportContainer(postImport);
        return odm;
    }
}
