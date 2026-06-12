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

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.crfdata.CRFDataPostImportContainer;
import org.researchedc.bean.submit.crfdata.FormDataBean;
import org.researchedc.bean.submit.crfdata.ImportItemDataBean;
import org.researchedc.bean.submit.crfdata.ImportItemGroupDataBean;
import org.researchedc.bean.submit.crfdata.ODMContainer;
import org.researchedc.bean.submit.crfdata.StudyEventDataBean;
import org.researchedc.bean.submit.crfdata.SubjectDataBean;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.spi.IItemDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.researchedc.dao.spi.ResponseSetDomainDao;
import org.researchedc.web.crfdata.ImportCRFDataService;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

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
                    mock(DataSource.class),
                    mock(AutowireCapableBeanFactory.class),
                    mock(IItemDataDAO.class),
                    mock(IItemDAO.class),
                    mock(IItemFormMetadataDAO.class),
                    mock(ResponseSetDomainDao.class),
                    locale -> mock(ImportCRFDataService.class));

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
        ImportCRFDataService importService = mock(ImportCRFDataService.class);
        ImportCrfDataAdapter adapter = new ImportCrfDataAdapter(
                mock(DataSource.class),
                mock(AutowireCapableBeanFactory.class),
                itemDataDao,
                mock(IItemDAO.class),
                mock(IItemFormMetadataDAO.class),
                mock(ResponseSetDomainDao.class),
                locale -> importService);

        ODMContainer odm = odmWithOneItem("I_WEIGHT", "70");
        EventCRFBean eventCrf = new EventCRFBean();
        eventCrf.setId(12);
        ItemDataBean itemData = new ItemDataBean();
        when(importService.fetchEventCRFBeans(any(), any())).thenReturn(List.of(eventCrf));
        when(importService.prepareItemDataForCommit(any(), any(), any(), any(Integer.class)))
                .thenReturn(itemData);
        when(itemDataDao.upsert(itemData)).thenThrow(new RuntimeException("database write failed"));

        assertThrows(IllegalStateException.class,
                () -> adapter.commitImport(new ImportCrfDataAdapter.ParsedOdm(odm), 1, Locale.ENGLISH));
        verify(itemDataDao).upsert(itemData);
    }

    private ODMContainer odmWithOneItem(String itemOid, String value) {
        ImportItemDataBean item = new ImportItemDataBean();
        item.setItemOID(itemOid);
        item.setValue(value);

        ImportItemGroupDataBean group = new ImportItemGroupDataBean();
        group.setItemData(new ArrayList<>(List.of(item)));

        FormDataBean form = new FormDataBean();
        form.setItemGroupData(new ArrayList<>(List.of(group)));

        StudyEventDataBean event = new StudyEventDataBean();
        event.setFormData(new ArrayList<>(List.of(form)));

        SubjectDataBean subject = new SubjectDataBean();
        subject.setStudyEventData(new ArrayList<>(List.of(event)));

        CRFDataPostImportContainer postImport = new CRFDataPostImportContainer();
        postImport.setSubjectData(new ArrayList<>(List.of(subject)));

        ODMContainer odm = new ODMContainer();
        odm.setCrfDataPostImportContainer(postImport);
        return odm;
    }
}
