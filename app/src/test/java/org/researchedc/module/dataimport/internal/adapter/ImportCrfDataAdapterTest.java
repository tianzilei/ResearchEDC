package org.researchedc.module.dataimport.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.researchedc.dao.spi.IItemDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.researchedc.dao.spi.ResponseSetDomainDao;
import org.researchedc.web.crfdata.ImportCRFDataService;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

class ImportCrfDataAdapterTest {

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
