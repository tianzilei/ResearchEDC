package org.researchedc.module.dataimport.internal.adapter;

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
import org.researchedc.bean.core.ItemDataType;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.submit.ResponseSetBean;
import org.researchedc.module.dataimport.internal.odm.FormDataBean;
import org.researchedc.module.dataimport.internal.odm.ImportItemDataBean;
import org.researchedc.module.dataimport.internal.odm.ImportItemGroupDataBean;
import org.researchedc.module.dataimport.internal.odm.ODMContainer;
import org.researchedc.module.dataimport.internal.odm.StudyEventDataBean;
import org.researchedc.module.dataimport.internal.odm.SubjectDataBean;
import org.researchedc.module.dataimport.internal.odm.UpsertOnBean;
import org.researchedc.control.form.DiscrepancyValidator;
import org.researchedc.control.form.FormDiscrepancyNotes;
import org.researchedc.control.form.Validator;
import org.researchedc.module.dataimport.service.ImportCrfVersionPort;
import org.researchedc.module.dataimport.service.ImportEventCrfPort;
import org.researchedc.module.dataimport.dto.ImportEventCrf;
import org.researchedc.module.dataimport.service.ImportItemDataPort;
import org.researchedc.module.dataimport.service.ImportItemFormMetadataPort;
import org.researchedc.module.dataimport.dto.ImportItemFormMetadata;
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
import org.researchedc.module.dataimport.internal.support.ImportCompatibilitySupport;
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
    private static final int STAGE_INVALID = 0;
    private static final int STAGE_INITIAL_DATA_ENTRY = 2;
    private static final int STAGE_INITIAL_DATA_ENTRY_COMPLETE = 3;
    private static final int STAGE_DOUBLE_DATA_ENTRY_COMPLETE = 5;
    private static final int STAGE_LOCKED = 7;

    private final ImportItemDataPort itemDataPort;
    private final ImportItemPort itemPort;
    private final ImportItemGroupPort itemGroupPort;
    private final ImportItemFormMetadataPort itemFormMetadataPort;
    private final ImportStudyLookupPort studyLookupPort;
    private final ImportStudySubjectPort studySubjectPort;
    private final ImportStudyEventPort studyEventPort;
    private final ImportStudyEventDefinitionPort studyEventDefinitionPort;
    private final ImportCrfVersionPort crfVersionPort;
    private final ImportEventCrfPort eventCrfPort;
    private final ImportResponseSetPort responseSetPort;

    ImportCrfDataAdapter(ImportItemDataPort itemDataPort, ImportItemPort itemPort,
                         ImportItemGroupPort itemGroupPort,
                         ImportItemFormMetadataPort itemFormMetadataPort,
                         ImportStudyLookupPort studyLookupPort,
                         ImportStudySubjectPort studySubjectPort,
                         ImportStudyEventPort studyEventPort,
                         ImportStudyEventDefinitionPort studyEventDefinitionPort,
                         ImportCrfVersionPort crfVersionPort,
                         ImportEventCrfPort eventCrfPort,
                         ImportResponseSetPort responseSetPort) {
        this.itemDataPort = itemDataPort;
        this.itemPort = itemPort;
        this.itemGroupPort = itemGroupPort;
        this.itemFormMetadataPort = itemFormMetadataPort;
        this.studyLookupPort = studyLookupPort;
        this.studySubjectPort = studySubjectPort;
        this.studyEventPort = studyEventPort;
        this.studyEventDefinitionPort = studyEventDefinitionPort;
        this.crfVersionPort = crfVersionPort;
        this.eventCrfPort = eventCrfPort;
        this.responseSetPort = responseSetPort;
    }

    private ODMContainer parseOdmContainer(Path filePath) {
        try {
            Mapping mapping = new Mapping();
            mapping.loadMapping(ImportCompatibilitySupport.odmMappingLocation());
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
        return validateStudyMetadata(parsed.odm(), studyId,
                ImportCompatibilitySupport.pageMessagesBundle(locale));
    }

    private ImportUser importUser(int studyId) {
        return new ImportUser(0, "system");
    }

    private boolean checkStatusesValid(ODMContainer odm, ImportUser user) {
        ArrayList<Integer> eventCrfBeanIds = new ArrayList<>();
        UpsertOnBean upsert = odm.getCrfDataPostImportContainer().getUpsertOn();
        if (upsert == null) {
            upsert = new UpsertOnBean();
        }
        String studyOid = odm.getCrfDataPostImportContainer().getStudyOID();
        ImportStudy study = importStudy(studyOid);
        ArrayList<SubjectDataBean> subjectDataBeans = odm.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            ImportStudySubject studySubjectBean =
                    importStudySubject(subjectDataBean.getSubjectOID(), study.id());
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null
                        ? "1"
                        : studyEventDataBean.getStudyEventRepeatKey();
                ImportStudyEventDefinition studyEventDefinitionBean =
                        importStudyEventDefinition(
                                studyEventDataBean.getStudyEventOID(),
                                study.id(),
                                study.parentStudyId());
                log.info("find all by def and subject {} study subject {}",
                        studyEventDefinitionBean.name(), studySubjectBean.name());

                ImportStudyEvent studyEventBean =
                        importStudyEvent(
                                studySubjectBean.id(),
                                studyEventDefinitionBean.id(),
                                Integer.parseInt(sampleOrdinal));
                SubjectEventStatus subjectEventStatus = subjectEventStatus(studyEventBean);
                if (subjectEventStatus.equals(SubjectEventStatus.LOCKED)
                        || subjectEventStatus.equals(SubjectEventStatus.SIGNED)
                        || subjectEventStatus.equals(SubjectEventStatus.STOPPED)) {
                    return true;
                }
                for (FormDataBean formDataBean : formDataBeans) {
                    List<ImportCrfVersionPort.ImportCrfVersion> crfVersionBeans =
                            findCrfVersionsByOid(formDataBean.getFormOID());
                    for (ImportCrfVersionPort.ImportCrfVersion crfVersionBean : crfVersionBeans) {
                        ArrayList<ImportEventCrf> eventCrfBeans =
                                eventCrfBeansByEventSubjectVersion(
                                        studyEventBean.id(), studySubjectBean.id(), crfVersionBean.id());
                        if (eventCrfBeans.isEmpty()) {
                            log.debug("found no event crfs from Study Event id {}, location {}",
                                    studyEventBean.id(), studyEventBean.location());
                            if (subjectEventStatus.equals(SubjectEventStatus.SCHEDULED)
                                    || subjectEventStatus.equals(SubjectEventStatus.DATA_ENTRY_STARTED)
                                    || subjectEventStatus.equals(SubjectEventStatus.COMPLETED)) {
                                if (!upsert.isNotStarted()) {
                                    return false;
                                }
                            }
                        }
                        for (ImportEventCrf eventCrfBean : eventCrfBeans) {
                            Integer ecbId = eventCrfBean.id();
                            if (!(stage(eventCrfBean) == STAGE_INITIAL_DATA_ENTRY
                                    && upsert.isDataEntryStarted())
                                    && !(stage(eventCrfBean) == STAGE_DOUBLE_DATA_ENTRY_COMPLETE
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

    private List<ImportEventCrf> getEventCrfBeans(ODMContainer odm, ImportUser user) {
        ArrayList<ImportEventCrf> eventCrfBeansResult = new ArrayList<>();
        ArrayList<Integer> eventCrfBeanIds = new ArrayList<>();
        UpsertOnBean upsert = odm.getCrfDataPostImportContainer().getUpsertOn();
        if (upsert == null) {
            upsert = new UpsertOnBean();
        }
        String studyOid = odm.getCrfDataPostImportContainer().getStudyOID();
        ImportStudy study = importStudy(studyOid);
        ArrayList<SubjectDataBean> subjectDataBeans = odm.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            ImportStudySubject studySubjectBean =
                    importStudySubject(subjectDataBean.getSubjectOID(), study.id());
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null
                        ? "1"
                        : studyEventDataBean.getStudyEventRepeatKey();
                ImportStudyEventDefinition studyEventDefinitionBean =
                        importStudyEventDefinition(
                                studyEventDataBean.getStudyEventOID(),
                                study.id(),
                                study.parentStudyId());
                log.info("find all by def and subject {} study subject {}",
                        studyEventDefinitionBean.name(), studySubjectBean.name());

                ImportStudyEvent studyEventBean =
                        importStudyEvent(
                                studySubjectBean.id(),
                                studyEventDefinitionBean.id(),
                                Integer.parseInt(sampleOrdinal));
                SubjectEventStatus subjectEventStatus = subjectEventStatus(studyEventBean);
                if (subjectEventStatus.equals(SubjectEventStatus.LOCKED)
                        || subjectEventStatus.equals(SubjectEventStatus.SIGNED)
                        || subjectEventStatus.equals(SubjectEventStatus.STOPPED)) {
                    return null;
                }
                for (FormDataBean formDataBean : formDataBeans) {
                    List<ImportCrfVersionPort.ImportCrfVersion> crfVersionBeans =
                            findCrfVersionsByOid(formDataBean.getFormOID());
                    for (ImportCrfVersionPort.ImportCrfVersion crfVersionBean : crfVersionBeans) {
                        ArrayList<ImportEventCrf> eventCrfBeans =
                                eventCrfBeansByEventSubjectVersion(
                                        studyEventBean.id(), studySubjectBean.id(), crfVersionBean.id());

                        if (eventCrfBeans.isEmpty()) {
                            eventCrfBeans = eventCrfBeansByEventSubjectCrfId(
                                    studyEventBean.id(), studySubjectBean.id(), crfVersionBean.id());
                            if (!eventCrfBeans.isEmpty()) {
                                eventCrfBeans.replaceAll(eventCrfBean -> new ImportEventCrf(
                                        eventCrfBean.id(), crfVersionBean.id(), eventCrfBean.statusId()));
                            }
                        }

                        if (eventCrfBeans.isEmpty()) {
                            log.debug("found no event crfs from Study Event id {}, location {}",
                                    studyEventBean.id(), studyEventBean.location());
                            if ((subjectEventStatus.equals(SubjectEventStatus.SCHEDULED)
                                    || subjectEventStatus.equals(SubjectEventStatus.DATA_ENTRY_STARTED)
                                    || subjectEventStatus.equals(SubjectEventStatus.COMPLETED))
                                    && upsert.isNotStarted()) {
                                ImportEventCrf newEventCrfBean = createImportEventCrf(
                                        studyEventBean,
                                        studySubjectBean,
                                        crfVersionBean,
                                        user);
                                log.debug("created and added new event crf");
                                if (!eventCrfBeanIds.contains(newEventCrfBean.id())) {
                                    eventCrfBeansResult.add(newEventCrfBean);
                                    eventCrfBeanIds.add(newEventCrfBean.id());
                                }
                            }
                        }

                        for (ImportEventCrf eventCrfBean : eventCrfBeans) {
                            Integer ecbId = eventCrfBean.id();
                            if ((upsert.isDataEntryStarted()
                                    && stage(eventCrfBean) == STAGE_INITIAL_DATA_ENTRY)
                                    || (upsert.isDataEntryComplete()
                                    && stage(eventCrfBean) == STAGE_DOUBLE_DATA_ENTRY_COMPLETE)) {
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
        ImportUser user = importUser(studyId);
        boolean statusesValid = checkStatusesValid(odm, user);
        List<ImportEventCrf> eventCrfBeans = getEventCrfBeans(odm, user);
        return new EventCrfValidationResult(statusesValid, eventCrfBeans != null ? eventCrfBeans.size() : -1);
    }

    public CommitResult commitImport(ParsedOdm parsed, int studyId, Locale locale) {
        ODMContainer odm = parsed.odm();
        ImportUser user = importUser(studyId);
        List<ImportEventCrf> eventCrfBeans = getEventCrfBeans(odm, user);
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
                                    ImportItemData itemData = prepareItemDataForCommit(
                                            importItem, eventCrfBeans.get(0), user, itemCount + 1);
                                    if (itemData != null) {
                                        itemDataPort.upsertImportItemData(
                                                itemData.itemId(),
                                                itemData.eventCrfId(),
                                                itemData.ordinal(),
                                                itemData.ownerId(),
                                                itemData.statusId(),
                                                itemData.value());
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
        ImportStudy study = importStudy(studyOid);
        if (study == null) {
            mf.applyPattern(respage.getString("your_study_oid_does_not_reference_an_existing"));
            errors.add(mf.format(new Object[]{studyOid}));
            log.debug("unknown study OID");
            return errors;
        }

        if (study.id() != currentStudyId) {
            mf.applyPattern(respage.getString("your_current_study_is_not_the_same_as"));
            errors.add(mf.format(new Object[]{study.name()}));
        }

        ArrayList<SubjectDataBean> subjectDataBeans =
                odmContainer.getCrfDataPostImportContainer().getSubjectData();
        if (subjectDataBeans == null) {
            return errors;
        }

        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            String subjectOid = subjectDataBean.getSubjectOID();
            ImportStudySubject studySubjectBean =
                    importStudySubject(subjectOid, study.id());
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
                ImportStudyEventDefinition studyEventDefinitionBean =
                        importStudyEventDefinition(
                                studyEventOid, study.id(), study.parentStudyId());
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
        List<ImportCrfVersionPort.ImportCrfVersion> crfVersionBeans = findCrfVersionsByOid(formOid);
        if (crfVersionBeans != null && !crfVersionBeans.isEmpty()) {
            for (ImportCrfVersionPort.ImportCrfVersion crfVersionBean : crfVersionBeans) {
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

    private ImportStudy importStudy(String studyOid) {
        return studyLookupPort.findImportStudyByOid(studyOid);
    }

    private ImportStudySubject importStudySubject(String subjectOid, int studyId) {
        return studySubjectPort.findImportStudySubjectByOidAndStudy(subjectOid, studyId);
    }

    private ImportStudyEventDefinition importStudyEventDefinition(String eventOid, int studyId, int parentStudyId) {
        return studyEventDefinitionPort.findImportStudyEventDefinitionByOidAndStudy(
                eventOid, studyId, parentStudyId);
    }

    private ImportStudyEvent importStudyEvent(int studySubjectId, int definitionId, int ordinal) {
        ImportStudyEvent row = studyEventPort.findImportStudyEventBySubjectDefinitionOrdinal(
                studySubjectId, definitionId, ordinal);
        if (row == null) {
            return new ImportStudyEvent(0, SubjectEventStatus.INVALID.getId(), null);
        }
        return row;
    }

    private List<ImportCrfVersionPort.ImportCrfVersion> findCrfVersionsByOid(String formOid) {
        List<ImportCrfVersionPort.ImportCrfVersion> versions =
                crfVersionPort.findAllImportCrfVersionsByOid(formOid);
        if (versions == null) {
            return null;
        }
        return versions;
    }

    private ArrayList<ImportEventCrf> eventCrfBeansByEventSubjectVersion(
            int studyEventId, int studySubjectId, int crfVersionId) {
        return new ArrayList<>(eventCrfPort.findImportEventCrfsByEventSubjectVersion(
                studyEventId, studySubjectId, crfVersionId));
    }

    private ArrayList<ImportEventCrf> eventCrfBeansByEventSubjectCrfId(
            int studyEventId, int studySubjectId, int crfVersionId) {
        return new ArrayList<>(eventCrfPort.findImportEventCrfsByEventSubjectCrfId(
                studyEventId, studySubjectId, crfVersionId));
    }

    private ImportEventCrf createImportEventCrf(
            ImportStudyEvent studyEvent,
            ImportStudySubject studySubject,
            ImportCrfVersionPort.ImportCrfVersion crfVersion,
            ImportUser user) {
        return eventCrfPort.createImportEventCrf(
                studyEvent.id(),
                studySubject.id(),
                crfVersion.id(),
                user.id(),
                user.name(),
                Status.AVAILABLE.getId());
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value.toString());
    }

    private void validateItemGroupMetadata(ImportItemGroupDataBean itemGroupDataBean, String formOid,
                                           List<String> errors, MessageFormat mf, ResourceBundle respage) {
        String itemGroupOid = itemGroupDataBean.getItemGroupOID();
        var itemGroups = itemGroupPort.findImportItemGroupsByOid(itemGroupOid);
        if (itemGroups != null) {
            log.debug("number of item group beans: {}", itemGroups.size());
            log.debug("item group oid: {}", itemGroupOid);
            for (var itemGroup : itemGroups) {
                if (itemGroup == null) {
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
        List<ImportItemPort.ImportItem> items = itemPort.findImportItemsByOid(itemOid);
        if (items == null) {
            return;
        }

        log.debug("found itembeans");
        for (ImportItemPort.ImportItem item : items) {
            if (item == null) {
                mf.applyPattern(respage.getString("your_item_oid_for_item_group_oid"));
                errors.add(mf.format(new Object[]{itemOid, itemGroupOid}));
            } else {
                log.debug("found {}, passing", item.oid());
            }
        }
    }

    private ImportItemData prepareItemDataForCommit(
            ImportItemDataBean importItem, ImportEventCrf eventCrf,
            ImportUser ub, int ordinal) {
        List<ImportItemPort.ImportItem> items = itemPort.findImportItemsByOid(importItem.getItemOID());
        if (items == null || items.isEmpty()) {
            return null;
        }

        return new ImportItemData(
                toInt(items.get(0).id()),
                toInt(eventCrf.id()),
                ordinal,
                ub.id(),
                Status.UNAVAILABLE.getId(),
                importItem.getValue());
    }

    private record ImportItemData(
            int itemId,
            int eventCrfId,
            int ordinal,
            int ownerId,
            int statusId,
            String value) {
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

        Map<String, ImportItemPort.ImportItem> itemCache = new HashMap<>();
        for (String oid : fieldValues.keySet()) {
            try {
                List<ImportItemPort.ImportItem> items = itemPort.findImportItemsByOid(oid);
                if (items != null && !items.isEmpty()) {
                    ImportItemPort.ImportItem item = items.get(0);
                    itemCache.put(oid, item);
                    ItemDataType dt = ItemDataType.get(toInt(item.dataTypeId()));
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

            ImportItemPort.ImportItem item = itemCache.get(oid);
            if (item != null) {
                ItemDataType dt = ItemDataType.get(toInt(item.dataTypeId()));
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
                        List<ImportItemFormMetadata> metas =
                                itemFormMetadataPort.findImportItemFormMetadataByItemId(toInt(item.id()));
                        if (metas != null && !metas.isEmpty()) {
                            ImportItemFormMetadata metadata = metas.get(0);
                            String wd = metadata.widthDecimal();
                            if (wd != null && !wd.isEmpty() && !"w(d)".equals(wd)) {
                                ArrayList<String> params = new ArrayList<>();
                                params.add(dt.getName());
                                params.add(wd);
                                dv.addValidation(oid, Validator.IS_VALID_WIDTH_DECIMAL, params);
                            }

                            // Response-set validation for controlled-vocabulary items
                            ResponseSetBean rsb = responseSetFromMetadata(metadata);
                            if (rsb == null) {
                                List<ImportResponseSetPort.ImportResponseSet> domainRsList =
                                        responseSetPort.findAllByItemId(toInt(item.id()));
                                if (domainRsList != null && !domainRsList.isEmpty()) {
                                    ImportResponseSetPort.ImportResponseSet domainRs = domainRsList.get(0);
                                    rsb = new ResponseSetBean();
                                    rsb.setResponseTypeId(domainRs.responseTypeId());
                                    rsb.setOptions(domainRs.optionsText(), domainRs.optionsValues());
                                }
                            }
                            if (rsb != null) {
                                int rt = rsb.getResponseTypeId();
                                if (rt == ResponseSetBean.RESPONSE_TYPE_RADIO
                                        || rt == ResponseSetBean.RESPONSE_TYPE_SELECT) {
                                    dv.addValidation(oid,
                                            Validator.IN_RESPONSE_SET_SINGLE_VALUE, rsb);
                                } else if (rt == ResponseSetBean.RESPONSE_TYPE_CHECKBOX
                                        || rt == ResponseSetBean.RESPONSE_TYPE_SELECT_MULTI) {
                                    dv.addValidation(oid,
                                            Validator.IN_RESPONSE_SET_COMMA_SEPERATED, rsb);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.debug("Metadata lookup skipped for item {}: {}",
                                item.id(), ex.getMessage());
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

    private ResponseSetBean responseSetFromMetadata(ImportItemFormMetadata metadata) {
        if (metadata.responseTypeId() == null) {
            return null;
        }
        ResponseSetBean rsb = new ResponseSetBean();
        rsb.setResponseTypeId(metadata.responseTypeId());
        rsb.setOptions(metadata.optionsText(), metadata.optionsValues());
        return rsb;
    }

    private SubjectEventStatus subjectEventStatus(ImportStudyEvent event) {
        return SubjectEventStatus.getFromMap(toInt(event.subjectEventStatusId()));
    }

    private int stage(ImportEventCrf eventCrf) {
        Status status = Status.getFromMap(toInt(eventCrf.statusId()));
        if (status == null) {
            return STAGE_INVALID;
        }
        if (status.equals(Status.AVAILABLE)) {
            return STAGE_INITIAL_DATA_ENTRY;
        }
        if (status.equals(Status.PENDING)) {
            return STAGE_INITIAL_DATA_ENTRY_COMPLETE;
        }
        if (status.equals(Status.UNAVAILABLE)) {
            return STAGE_DOUBLE_DATA_ENTRY_COMPLETE;
        }
        if (status.equals(Status.LOCKED)) {
            return STAGE_LOCKED;
        }
        return STAGE_INVALID;
    }

    private record ImportUser(int id, String name) {
    }

}
