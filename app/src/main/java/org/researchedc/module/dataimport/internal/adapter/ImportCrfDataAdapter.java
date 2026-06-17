package org.researchedc.module.dataimport.internal.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.researchedc.bean.core.DataEntryStage;
import org.researchedc.bean.core.ItemDataType;
import org.researchedc.bean.core.ResponseType;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.bean.submit.ResponseSetBean;
import org.researchedc.bean.submit.crfdata.FormDataBean;
import org.researchedc.bean.submit.crfdata.ImportItemDataBean;
import org.researchedc.bean.submit.crfdata.ImportItemGroupDataBean;
import org.researchedc.bean.submit.crfdata.ODMContainer;
import org.researchedc.bean.submit.crfdata.StudyEventDataBean;
import org.researchedc.bean.submit.crfdata.SubjectDataBean;
import org.researchedc.bean.submit.crfdata.UpsertOnBean;
import org.researchedc.control.form.DiscrepancyValidator;
import org.researchedc.control.form.FormDiscrepancyNotes;
import org.researchedc.control.form.Validator;
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
import org.researchedc.domain.datamap.ResponseSet;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final IItemDataDAO itemDataDao;
    private final IItemDAO itemDao;
    private final IItemGroupDAO itemGroupDao;
    private final IItemFormMetadataDAO itemFormMetadataDao;
    private final IStudyDAO studyDao;
    private final IStudySubjectDAO studySubjectDao;
    private final IStudyEventDAO studyEventDao;
    private final IStudyEventDefinitionDAO studyEventDefinitionDao;
    private final ICrfVersionDAO crfVersionDao;
    private final EventCRFDao eventCrfDao;
    private final ResponseSetDomainDao responseSetDomainDao;

    ImportCrfDataAdapter(IItemDataDAO itemDataDao, IItemDAO itemDao,
                         IItemGroupDAO itemGroupDao,
                         IItemFormMetadataDAO itemFormMetadataDao,
                         IStudyDAO studyDao,
                         IStudySubjectDAO studySubjectDao,
                         IStudyEventDAO studyEventDao,
                         IStudyEventDefinitionDAO studyEventDefinitionDao,
                         ICrfVersionDAO crfVersionDao,
                         EventCRFDao eventCrfDao,
                         ResponseSetDomainDao responseSetDomainDao) {
        this.itemDataDao = itemDataDao;
        this.itemDao = itemDao;
        this.itemGroupDao = itemGroupDao;
        this.itemFormMetadataDao = itemFormMetadataDao;
        this.studyDao = studyDao;
        this.studySubjectDao = studySubjectDao;
        this.studyEventDao = studyEventDao;
        this.studyEventDefinitionDao = studyEventDefinitionDao;
        this.crfVersionDao = crfVersionDao;
        this.eventCrfDao = eventCrfDao;
        this.responseSetDomainDao = responseSetDomainDao;
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
        ResourceBundleProvider.updateLocale(locale);
        return validateStudyMetadata(parsed.odm(), studyId,
                ResourceBundleProvider.getPageMessagesBundle(locale));
    }

    private UserAccountBean importUser(int studyId) {
        UserAccountBean user = new UserAccountBean();
        user.setActiveStudyId(studyId);
        user.setId(0);
        user.setName("system");
        return user;
    }

    private boolean checkStatusesValid(ODMContainer odm, UserAccountBean user) {
        ArrayList<Integer> eventCrfBeanIds = new ArrayList<>();
        UpsertOnBean upsert = odm.getCrfDataPostImportContainer().getUpsertOn();
        if (upsert == null) {
            upsert = new UpsertOnBean();
        }
        String studyOid = odm.getCrfDataPostImportContainer().getStudyOID();
        StudyBean studyBean = studyDao.findByOid(studyOid);
        ArrayList<SubjectDataBean> subjectDataBeans = odm.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            StudySubjectBean studySubjectBean =
                    studySubjectDao.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null
                        ? "1"
                        : studyEventDataBean.getStudyEventRepeatKey();
                StudyEventDefinitionBean studyEventDefinitionBean =
                        studyEventDefinitionDao.findByOidAndStudy(
                                studyEventDataBean.getStudyEventOID(),
                                studyBean.getId(),
                                studyBean.getParentStudyId());
                log.info("find all by def and subject {} study subject {}",
                        studyEventDefinitionBean.getName(), studySubjectBean.getName());

                StudyEventBean studyEventBean =
                        (StudyEventBean) studyEventDao.findByStudySubjectIdAndDefinitionIdAndOrdinal(
                                studySubjectBean.getId(),
                                studyEventDefinitionBean.getId(),
                                Integer.parseInt(sampleOrdinal));
                if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.LOCKED)
                        || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)
                        || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.STOPPED)) {
                    return true;
                }
                for (FormDataBean formDataBean : formDataBeans) {
                    ArrayList<CRFVersionBean> crfVersionBeans =
                            crfVersionDao.findAllByOid(formDataBean.getFormOID());
                    for (CRFVersionBean crfVersionBean : crfVersionBeans) {
                        ArrayList<EventCRFBean> eventCrfBeans =
                                eventCrfDao.findByEventSubjectVersion(
                                        studyEventBean, studySubjectBean, crfVersionBean);
                        if (eventCrfBeans.isEmpty()) {
                            log.debug("found no event crfs from Study Event id {}, location {}",
                                    studyEventBean.getId(), studyEventBean.getLocation());
                            if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.DATA_ENTRY_STARTED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED)) {
                                if (!upsert.isNotStarted()) {
                                    return false;
                                }
                            }
                        }
                        for (EventCRFBean eventCrfBean : eventCrfBeans) {
                            Integer ecbId = eventCrfBean.getId();
                            if (!(eventCrfBean.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY)
                                    && upsert.isDataEntryStarted())
                                    && !(eventCrfBean.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)
                                    && upsert.isDataEntryComplete())) {
                                return false;
                            }
                            if (!eventCrfBeanIds.contains(ecbId)) {
                                eventCrfBeanIds.add(ecbId);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private List<EventCRFBean> getEventCrfBeans(ODMContainer odm, UserAccountBean user) {
        ArrayList<EventCRFBean> eventCrfBeansResult = new ArrayList<>();
        ArrayList<Integer> eventCrfBeanIds = new ArrayList<>();
        UpsertOnBean upsert = odm.getCrfDataPostImportContainer().getUpsertOn();
        if (upsert == null) {
            upsert = new UpsertOnBean();
        }
        String studyOid = odm.getCrfDataPostImportContainer().getStudyOID();
        StudyBean studyBean = studyDao.findByOid(studyOid);
        ArrayList<SubjectDataBean> subjectDataBeans = odm.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            StudySubjectBean studySubjectBean =
                    studySubjectDao.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null
                        ? "1"
                        : studyEventDataBean.getStudyEventRepeatKey();
                StudyEventDefinitionBean studyEventDefinitionBean =
                        studyEventDefinitionDao.findByOidAndStudy(
                                studyEventDataBean.getStudyEventOID(),
                                studyBean.getId(),
                                studyBean.getParentStudyId());
                log.info("find all by def and subject {} study subject {}",
                        studyEventDefinitionBean.getName(), studySubjectBean.getName());

                StudyEventBean studyEventBean =
                        (StudyEventBean) studyEventDao.findByStudySubjectIdAndDefinitionIdAndOrdinal(
                                studySubjectBean.getId(),
                                studyEventDefinitionBean.getId(),
                                Integer.parseInt(sampleOrdinal));
                if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.LOCKED)
                        || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)
                        || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.STOPPED)) {
                    return null;
                }
                for (FormDataBean formDataBean : formDataBeans) {
                    ArrayList<CRFVersionBean> crfVersionBeans =
                            crfVersionDao.findAllByOid(formDataBean.getFormOID());
                    for (CRFVersionBean crfVersionBean : crfVersionBeans) {
                        ArrayList<EventCRFBean> eventCrfBeans =
                                eventCrfDao.findByEventSubjectVersion(
                                        studyEventBean, studySubjectBean, crfVersionBean);

                        if (eventCrfBeans.isEmpty()) {
                            eventCrfBeans = eventCrfDao.findByEventSubjectCRFid(
                                    studyEventBean, studySubjectBean, crfVersionBean);
                            if (!eventCrfBeans.isEmpty()) {
                                for (EventCRFBean eventCrfBean : eventCrfBeans) {
                                    eventCrfBean.setCRFVersionId(crfVersionBean.getId());
                                }
                            }
                        }

                        if (eventCrfBeans.isEmpty()) {
                            log.debug("found no event crfs from Study Event id {}, location {}",
                                    studyEventBean.getId(), studyEventBean.getLocation());
                            if ((studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.DATA_ENTRY_STARTED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED))
                                    && upsert.isNotStarted()) {
                                EventCRFBean newEventCrfBean = new EventCRFBean();
                                newEventCrfBean.setStudyEventId(studyEventBean.getId());
                                newEventCrfBean.setStudySubjectId(studySubjectBean.getId());
                                newEventCrfBean.setCRFVersionId(crfVersionBean.getId());
                                newEventCrfBean.setDateInterviewed(new Date());
                                newEventCrfBean.setOwner(user);
                                newEventCrfBean.setInterviewerName(user.getName());
                                newEventCrfBean.setCompletionStatusId(1);
                                newEventCrfBean.setStatus(Status.AVAILABLE);
                                newEventCrfBean.setStage(DataEntryStage.INITIAL_DATA_ENTRY);
                                newEventCrfBean = (EventCRFBean) eventCrfDao.create(newEventCrfBean);
                                log.debug("created and added new event crf");
                                if (!eventCrfBeanIds.contains(newEventCrfBean.getId())) {
                                    eventCrfBeansResult.add(newEventCrfBean);
                                    eventCrfBeanIds.add(newEventCrfBean.getId());
                                }
                            }
                        }

                        for (EventCRFBean eventCrfBean : eventCrfBeans) {
                            Integer ecbId = eventCrfBean.getId();
                            if ((upsert.isDataEntryStarted()
                                    && eventCrfBean.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY))
                                    || (upsert.isDataEntryComplete()
                                    && eventCrfBean.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE))) {
                                if (!eventCrfBeanIds.contains(ecbId)) {
                                    eventCrfBeansResult.add(eventCrfBean);
                                    eventCrfBeanIds.add(ecbId);
                                }
                            }
                        }
                    }
                }
            }
        }
        return eventCrfBeansResult;
    }

    public EventCrfValidationResult validateEventCrfs(ParsedOdm parsed, int studyId, Locale locale) {
        ODMContainer odm = parsed.odm();
        UserAccountBean user = importUser(studyId);
        boolean statusesValid = checkStatusesValid(odm, user);
        List<EventCRFBean> eventCrfBeans = getEventCrfBeans(odm, user);
        return new EventCrfValidationResult(statusesValid, eventCrfBeans != null ? eventCrfBeans.size() : -1);
    }

    public CommitResult commitImport(ParsedOdm parsed, int studyId, Locale locale) {
        ODMContainer odm = parsed.odm();
        UserAccountBean user = importUser(studyId);
        List<EventCRFBean> eventCrfBeans = getEventCrfBeans(odm, user);
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
                                    ItemDataBean idb = prepareItemDataForCommit(
                                            importItem, eventCrfBeans.get(0), user, itemCount + 1);
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

    /*
     * Mirrors the remaining legacy metadata checks while keeping them inside the module adapter boundary.
     */
    private List<String> validateStudyMetadata(
            ODMContainer odmContainer, int currentStudyId, ResourceBundle respage) {
        List<String> errors = new ArrayList<>();
        MessageFormat mf = new MessageFormat("");

        String studyOid = odmContainer.getCrfDataPostImportContainer().getStudyOID();
        StudyBean studyBean = studyDao.findByOid(studyOid);
        if (studyBean == null) {
            mf.applyPattern(respage.getString("your_study_oid_does_not_reference_an_existing"));
            errors.add(mf.format(new Object[]{studyOid}));
            log.debug("unknown study OID");
            return errors;
        }

        if (studyBean.getId() != currentStudyId) {
            mf.applyPattern(respage.getString("your_current_study_is_not_the_same_as"));
            errors.add(mf.format(new Object[]{studyBean.getName()}));
        }

        ArrayList<SubjectDataBean> subjectDataBeans =
                odmContainer.getCrfDataPostImportContainer().getSubjectData();
        if (subjectDataBeans == null) {
            return errors;
        }

        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            String subjectOid = subjectDataBean.getSubjectOID();
            StudySubjectBean studySubjectBean =
                    studySubjectDao.findByOidAndStudy(subjectOid, studyBean.getId());
            if (studySubjectBean == null) {
                mf.applyPattern(respage.getString("your_subject_oid_does_not_reference"));
                errors.add(mf.format(new Object[]{subjectOid}));
                log.debug("logged an error with subject oid {}", subjectOid);
            }

            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            if (studyEventDataBeans == null) {
                continue;
            }

            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                String studyEventOid = studyEventDataBean.getStudyEventOID();
                StudyEventDefinitionBean studyEventDefinitionBean =
                        studyEventDefinitionDao.findByOidAndStudy(
                                studyEventOid, studyBean.getId(), studyBean.getParentStudyId());
                if (studyEventDefinitionBean == null) {
                    mf.applyPattern(respage.getString("your_study_event_oid_for_subject_oid"));
                    errors.add(mf.format(new Object[]{studyEventOid, subjectOid}));
                    log.debug("logged an error with se oid {} and subject oid {}", studyEventOid, subjectOid);
                }

                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                if (formDataBeans == null) {
                    mf.applyPattern(respage.getString("your_study_event_contains_no_form_data"));
                    errors.add(mf.format(new Object[]{studyEventOid}));
                    continue;
                }

                for (FormDataBean formDataBean : formDataBeans) {
                    validateFormMetadata(formDataBean, studyEventOid, errors, mf, respage);
                }
            }
        }

        return errors;
    }

    private void validateFormMetadata(FormDataBean formDataBean, String studyEventOid,
                                      List<String> errors, MessageFormat mf, ResourceBundle respage) {
        String formOid = formDataBean.getFormOID();
        ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDao.findAllByOid(formOid);
        if (crfVersionBeans != null && !crfVersionBeans.isEmpty()) {
            for (CRFVersionBean crfVersionBean : crfVersionBeans) {
                if (crfVersionBean == null) {
                    mf.applyPattern(respage.getString("your_crf_version_oid_for_study_event_oid"));
                    errors.add(mf.format(new Object[]{formOid, studyEventOid}));
                    log.debug("logged an error with form {} and se oid {}", formOid, studyEventOid);
                }
            }
        } else {
            mf.applyPattern(respage.getString("your_crf_version_oid_did_not_generate"));
            errors.add(mf.format(new Object[]{formOid}));
        }

        ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
        if (itemGroupDataBeans == null) {
            mf.applyPattern(respage.getString("your_study_event_contains_no_form_data"));
            errors.add(mf.format(new Object[]{studyEventOid}));
            return;
        }

        for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
            validateItemGroupMetadata(itemGroupDataBean, formOid, errors, mf, respage);
        }
    }

    private void validateItemGroupMetadata(ImportItemGroupDataBean itemGroupDataBean, String formOid,
                                           List<String> errors, MessageFormat mf, ResourceBundle respage) {
        String itemGroupOid = itemGroupDataBean.getItemGroupOID();
        List<ItemGroupBean> itemGroupBeans = itemGroupDao.findAllByOid(itemGroupOid);
        if (itemGroupBeans != null) {
            log.debug("number of item group beans: {}", itemGroupBeans.size());
            log.debug("item group oid: {}", itemGroupOid);
            for (ItemGroupBean itemGroupBean : itemGroupBeans) {
                if (itemGroupBean == null) {
                    mf.applyPattern(respage.getString("your_item_group_oid_for_form_oid"));
                    errors.add(mf.format(new Object[]{itemGroupOid, formOid}));
                }
            }
        } else {
            mf.applyPattern(respage.getString("the_item_group_oid_did_not"));
            errors.add(mf.format(new Object[]{itemGroupOid}));
        }

        ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
        if (itemDataBeans == null) {
            mf.applyPattern(respage.getString("the_item_group_oid_did_not_contain_item_data"));
            errors.add(mf.format(new Object[]{itemGroupOid}));
            return;
        }

        for (ImportItemDataBean itemDataBean : itemDataBeans) {
            validateItemMetadata(itemDataBean, itemGroupOid, errors, mf, respage);
        }
    }

    private void validateItemMetadata(ImportItemDataBean itemDataBean, String itemGroupOid,
                                      List<String> errors, MessageFormat mf, ResourceBundle respage) {
        String itemOid = itemDataBean.getItemOID();
        List<ItemBean> itemBeans = itemDao.findByOid(itemOid);
        if (itemBeans == null) {
            return;
        }

        log.debug("found itembeans");
        for (ItemBean itemBean : itemBeans) {
            if (itemBean == null) {
                mf.applyPattern(respage.getString("your_item_oid_for_item_group_oid"));
                errors.add(mf.format(new Object[]{itemOid, itemGroupOid}));
            } else {
                log.debug("found {}, passing", itemBean.getOid());
            }
        }
    }

    private ItemDataBean prepareItemDataForCommit(
            ImportItemDataBean importItem, EventCRFBean eventCrf,
            UserAccountBean ub, int ordinal) {
        List<ItemBean> items = itemDao.findByOid(importItem.getItemOID());
        if (items == null || items.isEmpty()) {
            return null;
        }

        ItemDataBean itemDataBean = new ItemDataBean();
        itemDataBean.setItemId(items.get(0).getId());
        itemDataBean.setEventCRFId(eventCrf.getId());
        itemDataBean.setCreatedDate(new Date());
        itemDataBean.setOrdinal(ordinal);
        itemDataBean.setOwner(ub);
        itemDataBean.setStatus(Status.UNAVAILABLE);
        itemDataBean.setValue(importItem.getValue());
        return itemDataBean;
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
