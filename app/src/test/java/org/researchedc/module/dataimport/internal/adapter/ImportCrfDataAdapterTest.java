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
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.crfdata.CRFDataPostImportContainer;
import org.researchedc.bean.submit.crfdata.FormDataBean;
import org.researchedc.bean.submit.crfdata.ImportItemDataBean;
import org.researchedc.bean.submit.crfdata.ImportItemGroupDataBean;
import org.researchedc.bean.submit.crfdata.ODMContainer;
import org.researchedc.bean.submit.crfdata.StudyEventDataBean;
import org.researchedc.bean.submit.crfdata.SubjectDataBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.core.CoreResources;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IItemDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.researchedc.dao.spi.IItemGroupDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ResponseSetDomainDao;
import org.researchedc.bean.submit.CRFVersionBean;

class ImportCrfDataAdapterTest {

    @Test
    void parseOdm_readsRepresentativeClinicalDataFixture() throws Exception {
        CoreResources.ODM_MAPPING_DIR = Path.of("../shared/src/main/resources/properties")
                .toAbsolutePath()
                .toString();
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
                    mock(IItemDataDAO.class),
                    mock(IItemDAO.class),
                    mock(IItemGroupDAO.class),
                    mock(IItemFormMetadataDAO.class),
                    mock(IStudyDAO.class),
                    mock(IStudySubjectDAO.class),
                    mock(IStudyEventDAO.class),
                    mock(IStudyEventDefinitionDAO.class),
                    mock(ICrfVersionDAO.class),
                    mock(EventCRFDao.class),
                    mock(ResponseSetDomainDao.class));

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
        IItemDataDAO itemDataDao = mock(IItemDataDAO.class);
        IItemDAO itemDao = mock(IItemDAO.class);
        IStudyDAO studyDao = mock(IStudyDAO.class);
        IStudySubjectDAO studySubjectDao = mock(IStudySubjectDAO.class);
        IStudyEventDAO studyEventDao = mock(IStudyEventDAO.class);
        IStudyEventDefinitionDAO studyEventDefinitionDao = mock(IStudyEventDefinitionDAO.class);
        ICrfVersionDAO crfVersionDao = mock(ICrfVersionDAO.class);
        EventCRFDao eventCrfDao = mock(EventCRFDao.class);
        ImportCrfDataAdapter adapter = new ImportCrfDataAdapter(
                itemDataDao,
                itemDao,
                mock(IItemGroupDAO.class),
                mock(IItemFormMetadataDAO.class),
                studyDao,
                studySubjectDao,
                studyEventDao,
                studyEventDefinitionDao,
                crfVersionDao,
                eventCrfDao,
                mock(ResponseSetDomainDao.class));

        ODMContainer odm = odmWithOneItem("S_DEMO", "SS_DEMO", "SE_BASELINE", "F_VITALS_V1", "I_WEIGHT", "70");
        StudyBean study = new StudyBean();
        study.setId(1);
        study.setParentStudyId(0);
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
        EventCRFBean createdEventCrf = new EventCRFBean();
        createdEventCrf.setId(12);

        when(studyDao.findByOid("S_DEMO")).thenReturn(study);
        when(studySubjectDao.findByOidAndStudy("SS_DEMO", 1)).thenReturn(subject);
        when(studyEventDefinitionDao.findByOidAndStudy("SE_BASELINE", 1, 0)).thenReturn(definition);
        when(studyEventDao.findByStudySubjectIdAndDefinitionIdAndOrdinal(2, 3, 1)).thenReturn(event);
        when(crfVersionDao.findAllByOid("F_VITALS_V1")).thenReturn(new ArrayList<>(List.of(version)));
        when(eventCrfDao.findByEventSubjectVersion(event, subject, version)).thenReturn(new ArrayList<>());
        when(eventCrfDao.findByEventSubjectCRFid(event, subject, version)).thenReturn(new ArrayList<>());
        when(eventCrfDao.create(any(EventCRFBean.class))).thenReturn(createdEventCrf);
        ItemBean item = new ItemBean();
        item.setId(77);
        when(itemDao.findByOid("I_WEIGHT")).thenReturn(List.of(item));
        when(itemDataDao.upsert(any(ItemDataBean.class))).thenThrow(new RuntimeException("database write failed"));

        assertThrows(IllegalStateException.class,
                () -> adapter.commitImport(new ImportCrfDataAdapter.ParsedOdm(odm), 1, Locale.ENGLISH));
        verify(itemDataDao).upsert(any(ItemDataBean.class));
    }

    @Test
    void validateMetadata_whenStudyMissing_returnsLegacyErrorWithoutCallingLegacyService() {
        IStudyDAO studyDao = mock(IStudyDAO.class);
        when(studyDao.findByOid("S_MISSING")).thenReturn(null);

        ImportCrfDataAdapter adapter = new ImportCrfDataAdapter(
                mock(IItemDataDAO.class),
                mock(IItemDAO.class),
                mock(IItemGroupDAO.class),
                mock(IItemFormMetadataDAO.class),
                studyDao,
                mock(IStudySubjectDAO.class),
                mock(IStudyEventDAO.class),
                mock(IStudyEventDefinitionDAO.class),
                mock(ICrfVersionDAO.class),
                mock(EventCRFDao.class),
                mock(ResponseSetDomainDao.class));

        ODMContainer odm = new ODMContainer();
        CRFDataPostImportContainer postImport = new CRFDataPostImportContainer();
        postImport.setStudyOID("S_MISSING");
        odm.setCrfDataPostImportContainer(postImport);

        List<String> errors = adapter.validateMetadata(
                new ImportCrfDataAdapter.ParsedOdm(odm), 1, Locale.ENGLISH);

        assertEquals(1, errors.size());
        verify(studyDao).findByOid("S_MISSING");
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
