package org.researchedc.module.dataimport.internal.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import javax.sql.DataSource;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.researchedc.bean.core.ItemDataType;
import org.researchedc.bean.core.ResponseType;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.ResponseSetBean;
import org.researchedc.bean.submit.crfdata.ODMContainer;
import org.researchedc.control.form.DiscrepancyValidator;
import org.researchedc.control.form.FormDiscrepancyNotes;
import org.researchedc.control.form.Validator;
import org.researchedc.core.CoreResources;
import org.researchedc.dao.spi.IItemDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.researchedc.dao.spi.ResponseSetDomainDao;
import org.researchedc.domain.datamap.ResponseSet;
import org.researchedc.web.crfdata.ImportCRFDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class ImportCrfDataAdapter {

    public record ParsedOdm(ODMContainer odm) {
    }

    public record EventCrfValidationResult(boolean statusesValid, int eventCrfCount) {
    }

    public record CommitResult(int eventCrfCount, int itemCount) {
    }

    private static final Logger log = LoggerFactory.getLogger(ImportCrfDataAdapter.class);

    private final DataSource dataSource;
    private final AutowireCapableBeanFactory beanFactory;
    private final IItemDataDAO itemDataDao;
    private final IItemDAO itemDao;
    private final IItemFormMetadataDAO itemFormMetadataDao;
    private final ResponseSetDomainDao responseSetDomainDao;
    private final Function<Locale, ImportCRFDataService> serviceFactory;

    public ImportCrfDataAdapter(DataSource dataSource, AutowireCapableBeanFactory beanFactory,
                                IItemDataDAO itemDataDao, IItemDAO itemDao,
                                IItemFormMetadataDAO itemFormMetadataDao,
                                ResponseSetDomainDao responseSetDomainDao) {
        this(dataSource, beanFactory, itemDataDao, itemDao, itemFormMetadataDao,
                responseSetDomainDao, locale -> {
                    log.debug("Creating legacy ImportCRFDataService adapter for locale: {}", locale);
                    ImportCRFDataService service = new ImportCRFDataService(dataSource, locale);
                    beanFactory.autowireBean(service);
                    return service;
                });
    }

    ImportCrfDataAdapter(DataSource dataSource, AutowireCapableBeanFactory beanFactory,
                         IItemDataDAO itemDataDao, IItemDAO itemDao,
                         IItemFormMetadataDAO itemFormMetadataDao,
                         ResponseSetDomainDao responseSetDomainDao,
                         Function<Locale, ImportCRFDataService> serviceFactory) {
        this.dataSource = dataSource;
        this.beanFactory = beanFactory;
        this.itemDataDao = itemDataDao;
        this.itemDao = itemDao;
        this.itemFormMetadataDao = itemFormMetadataDao;
        this.responseSetDomainDao = responseSetDomainDao;
        this.serviceFactory = serviceFactory;
    }

    public ImportCRFDataService createService(Locale locale) {
        return serviceFactory.apply(locale);
    }

    private ODMContainer parseOdmContainer(Path filePath) {
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

    public ParsedOdm parseOdm(Path filePath) {
        return new ParsedOdm(parseOdmContainer(filePath));
    }

    public List<String> validateMetadata(ParsedOdm parsed, int studyId, Locale locale) {
        ODMContainer odm = parsed.odm();
        ImportCRFDataService service = createService(locale);
        return service.validateStudyMetadata(odm, studyId);
    }

    private boolean checkStatusesValid(ODMContainer odm, int studyId, Locale locale) {
        ImportCRFDataService service = createService(locale);
        UserAccountBean ub = new UserAccountBean();
        ub.setActiveStudyId(studyId);
        ub.setId(0);
        ub.setName("system");
        return service.eventCRFStatusesValid(odm, ub);
    }

    private List<EventCRFBean> getEventCrfBeans(ODMContainer odm, int studyId, Locale locale) {
        ImportCRFDataService service = createService(locale);
        UserAccountBean ub = new UserAccountBean();
        ub.setActiveStudyId(studyId);
        ub.setId(0);
        ub.setName("system");
        return service.fetchEventCRFBeans(odm, ub);
    }

    public EventCrfValidationResult validateEventCrfs(ParsedOdm parsed, int studyId, Locale locale) {
        ODMContainer odm = parsed.odm();
        boolean statusesValid = checkStatusesValid(odm, studyId, locale);
        List<EventCRFBean> eventCrfBeans = getEventCrfBeans(odm, studyId, locale);
        return new EventCrfValidationResult(statusesValid, eventCrfBeans != null ? eventCrfBeans.size() : -1);
    }

    public CommitResult commitImport(ParsedOdm parsed, int studyId, Locale locale) {
        ODMContainer odm = parsed.odm();
        ImportCRFDataService service = createService(locale);
        UserAccountBean ub = new UserAccountBean();
        ub.setActiveStudyId(studyId);
        ub.setId(0);
        ub.setName("system");
        List<EventCRFBean> eventCrfBeans = service.fetchEventCRFBeans(odm, ub);
        if (eventCrfBeans == null || eventCrfBeans.isEmpty()) {
            return new CommitResult(0, 0);
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
                                    throw new IllegalStateException(
                                            "Failed to persist imported item "
                                                    + importItem.getItemOID() + ": "
                                                    + e.getMessage(),
                                            e);
                                }
                            }
                        }
                    }
                }
            }
        }
        log.info("Commit: created/updated {} event CRFs and persisted {} items for study {}",
                eventCrfBeans.size(), itemCount, studyId);
        return new CommitResult(eventCrfBeans.size(), itemCount);
    }

    public String validateEditChecks(ParsedOdm parsed, int studyId) {
        ODMContainer odm = parsed.odm();
        Map<String, String> fieldValues = new HashMap<>();
        int totalItems = 0;
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
                                totalItems++;
                                String oid = importItem.getItemOID();
                                String value = importItem.getValue();
                                fieldValues.put(oid, value != null ? value : "");
                            }
                        }
                    }
                }
            }
        }

        Map<String, ItemBean> itemCache = new HashMap<>();
        for (String oid : fieldValues.keySet()) {
            try {
                List<ItemBean> items = itemDao.findByOid(oid);
                if (items != null && !items.isEmpty()) {
                    ItemBean item = items.get(0);
                    itemCache.put(oid, item);
                    ItemDataType dt = item.getDataType();
                    if (dt == ItemDataType.DATE) {
                        String value = fieldValues.get(oid);
                        if (value != null && !value.isEmpty()) {
                            try {
                                SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd");
                                isoFmt.setLenient(false);
                                Date parsedDate = isoFmt.parse(value);
                                SimpleDateFormat targetFmt = new SimpleDateFormat("MM/dd/yyyy");
                                targetFmt.setLenient(false);
                                fieldValues.put(oid, targetFmt.format(parsedDate));
                            } catch (ParseException e) {
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Item lookup skipped for OID {}: {}", oid, e.getMessage());
            }
        }

        FormDiscrepancyNotes notes = new FormDiscrepancyNotes();
        DiscrepancyValidator dv = new DiscrepancyValidator(fieldValues, Locale.ENGLISH, notes);

        for (String oid : fieldValues.keySet()) {
            dv.addValidation(oid, Validator.NO_BLANKS);

            ItemBean item = itemCache.get(oid);
            if (item != null) {
                ItemDataType dt = item.getDataType();
                if (dt != null) {
                    if (dt == ItemDataType.REAL) {
                        dv.addValidation(oid, Validator.IS_A_NUMBER);
                    } else if (dt == ItemDataType.INTEGER) {
                        dv.addValidation(oid, Validator.IS_AN_INTEGER);
                    } else if (dt == ItemDataType.DATE) {
                        dv.addValidation(oid, Validator.IS_A_DATE);
                    } else if (dt == ItemDataType.PDATE) {
                        dv.addValidation(oid, Validator.IS_PARTIAL_DATE);
                    }

                    try {
                        ArrayList<ItemFormMetadataBean> metas =
                                itemFormMetadataDao.findAllByItemId(item.getId());
                        if (metas != null && !metas.isEmpty()) {
                            String wd = metas.get(0).getWidthDecimal();
                            if (wd != null && !wd.isEmpty() && !"w(d)".equals(wd)) {
                                ArrayList<String> params = new ArrayList<>();
                                params.add(dt.getName());
                                params.add(wd);
                                dv.addValidation(oid, Validator.IS_VALID_WIDTH_DECIMAL, params);
                            }

                            // Response-set validation for controlled-vocabulary items
                            ResponseSetBean rsb = metas.get(0).getResponseSet();
                            if (rsb == null) {
                                List<ResponseSet> domainRsList =
                                        responseSetDomainDao.findAllByItemId(item.getId());
                                if (domainRsList != null && !domainRsList.isEmpty()) {
                                    ResponseSet domainRs = domainRsList.get(0);
                                    rsb = new ResponseSetBean();
                                    rsb.setResponseTypeId(
                                            domainRs.getResponseType().getResponseTypeId());
                                    rsb.setOptions(domainRs.getOptionsText(),
                                            domainRs.getOptionsValues());
                                }
                            }
                            if (rsb != null) {
                                ResponseType rt = rsb.getResponseType();
                                if (rt == ResponseType.RADIO || rt == ResponseType.SELECT) {
                                    dv.addValidation(oid,
                                            Validator.IN_RESPONSE_SET_SINGLE_VALUE, rsb);
                                } else if (rt == ResponseType.CHECKBOX
                                        || rt == ResponseType.SELECTMULTI) {
                                    dv.addValidation(oid,
                                            Validator.IN_RESPONSE_SET_COMMA_SEPERATED, rsb);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.debug("Metadata lookup skipped for item {}: {}",
                                item.getId(), ex.getMessage());
                    }


                }
            }
        }

        var errors = dv.validate();
        int errorCount = errors.size();
        log.info("Edit check validation for study {}: total={}, errors={}, passed={}",
                studyId, totalItems, errorCount, totalItems - errorCount);
        return "{\"total\":" + totalItems + ",\"errors\":" + errorCount + "}";
    }
}
