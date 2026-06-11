package org.researchedc.module.dataimport.internal.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.crfdata.ODMContainer;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.web.crfdata.ImportCRFDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class ImportCrfDataAdapter {

    private static final Logger log = LoggerFactory.getLogger(ImportCrfDataAdapter.class);

    private final DataSource dataSource;
    private final AutowireCapableBeanFactory beanFactory;
    private final IItemDataDAO itemDataDao;

    public ImportCrfDataAdapter(DataSource dataSource, AutowireCapableBeanFactory beanFactory,
                                IItemDataDAO itemDataDao) {
        this.dataSource = dataSource;
        this.beanFactory = beanFactory;
        this.itemDataDao = itemDataDao;
    }

    public ImportCRFDataService createService(Locale locale) {
        log.debug("Creating legacy ImportCRFDataService adapter for locale: {}", locale);
        ImportCRFDataService service = new ImportCRFDataService(dataSource, locale);
        beanFactory.autowireBean(service);
        return service;
    }

    public ODMContainer parseOdm(Path filePath) {
        try {
            String mappingDir = CoreResources.ODM_MAPPING_DIR;
            Mapping mapping = new Mapping();
            mapping.loadMapping(mappingDir + File.separator + "cd_odm_mapping.xml");
            Unmarshaller unmarshaller = new Unmarshaller(mapping);
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
                return (ODMContainer) unmarshaller.unmarshal(reader);
            }
        } catch (Exception e) {
            log.error("Failed to parse ODM file: {}", filePath, e);
            throw new RuntimeException("ODM parsing failed: " + e.getMessage(), e);
        }
    }

    public List<String> validateMetadata(ODMContainer odm, int studyId, Locale locale) {
        ImportCRFDataService service = createService(locale);
        return service.validateStudyMetadata(odm, studyId);
    }

    public boolean checkStatusesValid(ODMContainer odm, int studyId, Locale locale) {
        ImportCRFDataService service = createService(locale);
        UserAccountBean ub = new UserAccountBean();
        ub.setActiveStudyId(studyId);
        ub.setId(0);
        ub.setName("system");
        return service.eventCRFStatusesValid(odm, ub);
    }

    public List<EventCRFBean> getEventCrfBeans(ODMContainer odm, int studyId, Locale locale) {
        ImportCRFDataService service = createService(locale);
        UserAccountBean ub = new UserAccountBean();
        ub.setActiveStudyId(studyId);
        ub.setId(0);
        ub.setName("system");
        return service.fetchEventCRFBeans(odm, ub);
    }

    public int commitImport(ODMContainer odm, int studyId, Locale locale) {
        ImportCRFDataService service = createService(locale);
        UserAccountBean ub = new UserAccountBean();
        ub.setActiveStudyId(studyId);
        ub.setId(0);
        ub.setName("system");
        List<EventCRFBean> eventCrfBeans = service.fetchEventCRFBeans(odm, ub);
        if (eventCrfBeans == null || eventCrfBeans.isEmpty()) {
            return 0;
        }

        int itemCount = 0;
        var postImport = odm.getCrfDataPostImportContainer();
        if (postImport != null && postImport.getSubjectData() != null) {
            for (var subjectData : postImport.getSubjectData()) {
                if (subjectData.getStudyEventData() == null) continue;
                for (var eventData : subjectData.getStudyEventData()) {
                    if (eventData.getFormData() == null) continue;
                    for (var formData : eventData.getFormData()) {
                        if (formData.getItemGroupData() == null) continue;
                        for (var groupData : formData.getItemGroupData()) {
                            if (groupData.getItemData() == null) continue;
                            for (var importItem : groupData.getItemData()) {
                                try {
                                    ItemDataBean idb = service.prepareItemDataForCommit(
                                            importItem, eventCrfBeans.get(0),
                                            ub, itemCount + 1);
                                    if (idb != null) {
                                        itemDataDao.upsert(idb);
                                        itemCount++;
                                    }
                                } catch (Exception e) {
                                    log.warn("Failed to persist item {}: {}",
                                            importItem.getItemOID(), e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
        log.info("Commit: created/updated {} event CRFs and persisted {} items for study {}",
                eventCrfBeans.size(), itemCount, studyId);
        return eventCrfBeans.size();
    }
}
