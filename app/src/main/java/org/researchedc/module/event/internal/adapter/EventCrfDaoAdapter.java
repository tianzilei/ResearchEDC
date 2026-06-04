package org.researchedc.module.event.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.dao.EventCRFSDVFilter;
import org.researchedc.dao.EventCRFSDVSort;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("eventCRFDAO")
@Primary
@Transactional(readOnly = true)
public class EventCrfDaoAdapter implements EventCRFDao {

    private final EventCrfRepository eventCrfRepository;

    public EventCrfDaoAdapter(EventCrfRepository eventCrfRepository) {
        this.eventCrfRepository = eventCrfRepository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return eventCrfRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(EventCRFBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        EventCRFBean bean = (EventCRFBean) eb;
        EventCrfEntity entity = new EventCrfEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(eventCrfRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        EventCRFBean bean = (EventCRFBean) eb;
        EventCrfEntity entity = eventCrfRepository.findById(bean.getId())
                .orElseGet(EventCrfEntity::new);
        entity.setEventCrfId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(eventCrfRepository.save(entity));
    }

    @Override
    public Collection findAll() {
        return toBeans(eventCrfRepository.findAll());
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn,
                                          boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        EventCrfEntity entity = new EventCrfEntity();
        entity.setEventCrfId((Integer) hm.get("event_crf_id"));
        entity.setStudyEventId((Integer) hm.get("study_event_id"));
        entity.setStudySubjectId((Integer) hm.get("study_subject_id"));
        entity.setCrfVersionId((Integer) hm.get("crf_version_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setDateInterviewed(toLocalDateTime((Date) hm.get("date_interviewed")));
        entity.setInterviewerName((String) hm.get("interviewer_name"));
        entity.setAnnotations((String) hm.get("annotations"));
        entity.setDateCompleted(toLocalDateTime((Date) hm.get("date_completed")));
        entity.setValidatorId((Integer) hm.get("validator_id"));
        entity.setDateValidate(toLocalDateTime((Date) hm.get("date_validate")));
        entity.setDateValidateCompleted(toLocalDateTime((Date) hm.get("date_validate_completed")));
        entity.setValidatorAnnotations((String) hm.get("validator_annotations"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setElectronicSignatureStatus((Boolean) hm.get("electronic_signature_status"));
        entity.setSdvStatus((Boolean) hm.get("sdv_status"));
        entity.setOldStatusId((Integer) hm.get("old_status_id"));
        entity.setSdvUpdateId((Integer) hm.get("sdv_update_id"));
        return toBean(entity);
    }

    @Override
    public ArrayList findAllByStudyEvent(StudyEventBean studyEvent) {
        return toBeans(eventCrfRepository.findByStudyEventId(studyEvent.getId()));
    }

    @Override
    public ArrayList findAllByStudyEventAndStatus(StudyEventBean studyEvent, Status status) {
        return toBeans(eventCrfRepository.findByStudyEventIdAndStatusId(
                studyEvent.getId(), status.getId()));
    }

    @Override
    public ArrayList<EventCRFBean> findAllByStudySubject(int studySubjectId) {
        return toBeans(eventCrfRepository.findByStudySubjectId(studySubjectId));
    }

    @Override
    public ArrayList findAllByStudyEventAndCrfOrCrfVersionOid(StudyEventBean studyEvent, String crfVersionOrCrfOID) {
        try {
            return toBeans(eventCrfRepository.findByStudyEventIdAndCrfVersionId(
                    studyEvent.getId(), Integer.parseInt(crfVersionOrCrfOID)));
        } catch (NumberFormatException e) {
            return new ArrayList();
        }
    }

    @Override
    public ArrayList<EventCRFBean> findAllByStudyEventInParticipantForm(StudyEventBean studyEvent,
                                                                        int sed_Id, int studyId) {
        return new ArrayList();
    }

    @Override
    public ArrayList<EventCRFBean> findAllByStudyEventDefinition(int sed_Id, int studyId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllByCRF(int crfId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllByCRFVersion(int versionId) {
        return toBeans(eventCrfRepository.findByCrfVersionId(versionId));
    }

    @Override
    public ArrayList findAllStudySubjectByCRFVersion(int versionId) {
        return toBeans(eventCrfRepository.findByCrfVersionId(versionId));
    }

    @Override
    public ArrayList findUndeletedWithStudySubjectsByCRFVersion(int versionId) {
        return toBeans(eventCrfRepository.findByCrfVersionId(versionId).stream()
                .filter(e -> e.getStatusId() == null
                        || (e.getStatusId() != Status.DELETED.getId()
                                && e.getStatusId() != Status.AUTO_DELETED.getId()))
                .collect(Collectors.toList()));
    }

    @Override
    public ArrayList findByEventSubjectVersion(StudyEventBean studyEvent, StudySubjectBean studySubject,
                                               CRFVersionBean crfVersion) {
        return toBeans(eventCrfRepository.findByStudyEventIdAndStudySubjectIdAndCrfVersionId(
                studyEvent.getId(), studySubject.getId(), crfVersion.getId()));
    }

    @Override
    public EventCRFBean findByEventCrfVersion(StudyEventBean studyEvent, CRFVersionBean crfVersion) {
        return eventCrfRepository.findByStudyEventIdAndCrfVersionId(
                        studyEvent.getId(), crfVersion.getId()).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public ArrayList<EventCRFBean> findByCrfVersion(CRFVersionBean crfVersion) {
        return toBeans(eventCrfRepository.findByCrfVersionId(crfVersion.getId()));
    }

    @Override
    @Transactional
    public void delete(int eventCRFId) {
    }

    @Override
    @Transactional
    public void setSDVStatus(boolean sdvStatus, int userId, int eventCRFId) {
    }

    @Override
    @Transactional
    public void markComplete(EventCRFBean ecb, boolean ide) {
    }

    @Override
    @Transactional
    public void updateCRFVersionID(int event_crf_id, int crf_version_id, int user_id) {
    }

    @Override
    @Transactional
    public void updateCRFVersionID(int event_crf_id, int crf_version_id, int user_id, java.sql.Connection con) {
    }

    @Override
    public ArrayList findByEventSubjectCRFid(StudyEventBean studyEvent, StudySubjectBean studySubject,
                                             CRFVersionBean crfVersion) {
        return toBeans(eventCrfRepository.findByStudyEventIdAndStudySubjectIdAndCrfVersionId(
                studyEvent.getId(), studySubject.getId(), crfVersion.getId()));
    }

    @Override
    public EventCRFBean findByEventCrfID(StudyEventBean studyEvent, CRFVersionBean crfVersion) {
        return eventCrfRepository.findByStudyEventIdAndCrfVersionId(
                        studyEvent.getId(), crfVersion.getId()).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public Map<Integer, SortedSet<EventCRFBean>> buildEventCrfListByStudyEvent(Integer studySubjectId) {
        return new HashMap();
    }

    @Override
    public Set<Integer> buildNonEmptyEventCrfIds(Integer studySubjectId) {
        return new java.util.LinkedHashSet();
    }

    @Override
    public List<EventCRFBean> findAllCRFMigrationReportList(CRFVersionBean sourceCrfVersionBean,
                                                            CRFVersionBean targetCrfVersionBean,
                                                            ArrayList<String> studyEventDefnlist,
                                                            ArrayList<String> sitelist) {
        return new ArrayList();
    }

    @Override
    public Integer countEventCRFsByEventNameSubjectLabel(String eventName, String subjectLabel) {
        return 0;
    }

    @Override
    public AuditableEntityBean findByPKAndStudy(int id, StudyBean study) {
        return null;
    }

    @Override
    public boolean isQuerySuccessful() {
        return true;
    }

    @Override
    public Integer countEventCRFsByStudySubject(int studySubjectId, int studyId, int parentStudyId) {
        return 0;
    }

    @Override
    public ArrayList getEventCRFsByStudySubjectCompleteOrLocked(int studySubjectId) {
        return new ArrayList();
    }

    @Override
    public ArrayList getEventCRFsByStudySubjectLimit(int studySubjectId, int studyId, int parentStudyId,
                                                     int limit, int offset) {
        return new ArrayList();
    }

    @Override
    public ArrayList getEventCRFsByStudySubject(int studySubjectId, int studyId, int parentStudyId) {
        return new ArrayList();
    }

    @Override
    public Integer getCountWithFilter(int studyId, int parentStudyId, EventCRFSDVFilter filter) {
        return 0;
    }

    @Override
    public ArrayList<EventCRFBean> getWithFilterAndSort(int studyId, int parentStudyId, EventCRFSDVFilter filter,
                                                        EventCRFSDVSort sort, int rowStart, int rowEnd) {
        return new ArrayList();
    }

    @Override
    public ArrayList getEventCRFsByStudySubjectLabelLimit(String label, int studyId, int parentStudyId,
                                                          int limit, int offset) {
        return new ArrayList();
    }

    @Override
    public Integer countEventCRFsByStudySubjectLabel(String label, int studyId, int parentStudyId) {
        return 0;
    }

    private void apply(EventCRFBean bean, EventCrfEntity entity) {
        entity.setStudyEventId(bean.getStudyEventId() > 0 ? bean.getStudyEventId() : null);
        entity.setStudySubjectId(bean.getStudySubjectId() > 0 ? bean.getStudySubjectId() : null);
        entity.setCrfVersionId(bean.getCRFVersionId() > 0 ? bean.getCRFVersionId() : null);
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setDateInterviewed(toLocalDateTime(bean.getDateInterviewed()));
        entity.setInterviewerName(bean.getInterviewerName());
        entity.setAnnotations(bean.getAnnotations());
        entity.setDateCompleted(toLocalDateTime(bean.getDateCompleted()));
        entity.setValidatorId(bean.getValidatorId() > 0 ? bean.getValidatorId() : null);
        entity.setDateValidate(toLocalDateTime(bean.getDateValidate()));
        entity.setDateValidateCompleted(toLocalDateTime(bean.getDateValidateCompleted()));
        entity.setValidatorAnnotations(bean.getValidatorAnnotations());
        entity.setOwnerId(bean.getOwnerId() > 0 ? bean.getOwnerId() : null);
        entity.setUpdateId(bean.getUpdaterId() > 0 ? bean.getUpdaterId() : null);
        entity.setElectronicSignatureStatus(bean.isElectronicSignatureStatus());
        entity.setSdvStatus(bean.isSdvStatus());
        entity.setSdvUpdateId(bean.getSdvUpdateId() > 0 ? bean.getSdvUpdateId() : null);
    }

    private ArrayList<EventCRFBean> toBeans(List<EventCrfEntity> entities) {
        ArrayList<EventCRFBean> beans = new ArrayList();
        entities.stream()
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private EventCRFBean toBean(EventCrfEntity entity) {
        EventCRFBean bean = new EventCRFBean();
        if (entity.getEventCrfId() != null) {
            bean.setId(entity.getEventCrfId());
        }
        bean.setStudyEventId(valueOrZero(entity.getStudyEventId()));
        bean.setStudySubjectId(valueOrZero(entity.getStudySubjectId()));
        bean.setCRFVersionId(valueOrZero(entity.getCrfVersionId()));
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setDateInterviewed(toDate(entity.getDateInterviewed()));
        bean.setInterviewerName(entity.getInterviewerName() != null ? entity.getInterviewerName() : "");
        bean.setAnnotations(entity.getAnnotations() != null ? entity.getAnnotations() : "");
        bean.setDateCompleted(toDate(entity.getDateCompleted()));
        bean.setValidatorId(valueOrZero(entity.getValidatorId()));
        bean.setDateValidate(toDate(entity.getDateValidate()));
        bean.setDateValidateCompleted(toDate(entity.getDateValidateCompleted()));
        bean.setValidatorAnnotations(entity.getValidatorAnnotations() != null ? entity.getValidatorAnnotations() : "");
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setElectronicSignatureStatus(entity.getElectronicSignatureStatus() != null
                && entity.getElectronicSignatureStatus());
        bean.setSdvStatus(entity.getSdvStatus() != null && entity.getSdvStatus());
        bean.setSdvUpdateId(valueOrZero(entity.getSdvUpdateId()));
        return bean;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return new Date(0);
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }
}
