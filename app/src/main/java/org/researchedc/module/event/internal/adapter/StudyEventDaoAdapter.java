package org.researchedc.module.event.internal.adapter;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.domain.datamap.StudyEvent;
import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.researchedc.patterns.ocobserver.Observer;
import org.researchedc.patterns.ocobserver.StudyEventContainer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("studyEventDAO")
@Primary
@Transactional(readOnly = true)
public class StudyEventDaoAdapter implements IStudyEventDAO {

    private final StudyEventRepository studyEventRepository;

    public StudyEventDaoAdapter(StudyEventRepository studyEventRepository) {
        this.studyEventRepository = studyEventRepository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return studyEventRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(StudyEventBean::new);
    }

    @Override
    public EntityBean findByPKCached(int ID) {
        return findByPK(ID);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        StudyEventBean bean = (StudyEventBean) eb;
        StudyEventEntity entity = new StudyEventEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(studyEventRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb, boolean isTransaction) {
        return create(eb);
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        StudyEventBean bean = (StudyEventBean) eb;
        StudyEventEntity entity = studyEventRepository.findById(bean.getId())
                .orElseGet(StudyEventEntity::new);
        if (bean.getId() > 0) {
            entity.setStudyEventId(bean.getId());
        }
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(studyEventRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb, boolean isTransaction) {
        return update(eb);
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb, Connection con) {
        return update(eb);
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb, Connection con, boolean isTransaction) {
        return update(eb);
    }

    @Override
    public Collection findAll() {
        return toBeans(studyEventRepository.findAll());
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
        StudyEventEntity entity = new StudyEventEntity();
        entity.setStudyEventId((Integer) hm.get("study_event_id"));
        entity.setStudySubjectId((Integer) hm.get("study_subject_id"));
        entity.setStudyEventDefinitionId((Integer) hm.get("study_event_definition_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setSubjectEventStatusId((Integer) hm.get("subject_event_status_id"));
        entity.setLocation((String) hm.get("location"));
        entity.setSampleOrdinal((Integer) hm.get("sample_ordinal"));
        entity.setDateStart(toLocalDateTime((Date) hm.get("date_start")));
        entity.setDateEnd(toLocalDateTime((Date) hm.get("date_end")));
        entity.setStartTimeFlag((Boolean) hm.get("start_time_flag"));
        entity.setEndTimeFlag((Boolean) hm.get("end_time_flag"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setSedOrdinal((Integer) hm.get("sed_ordinal"));
        return toBean(entity);
    }

    @Override
    public Collection findAllByDefinition(int definitionId) {
        return toBeans(studyEventRepository.findByStudyEventDefinitionId(definitionId));
    }

    @Override
    public ArrayList findAllByStudyEventDefinitionAndCrfOids(String studyEventDefinitionOid, String crfOrCrfVersionOid) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllWithSubjectLabelByDefinition(int definitionId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllWithSubjectLabelByStudySubjectAndDefinition(StudySubjectBean studySubject, int definitionId) {
        return new ArrayList();
    }

    @Override
    public EntityBean findByStudySubjectIdAndDefinitionIdAndOrdinal(int ssbid, int sedid, int ord) {
        List<StudyEventEntity> results = studyEventRepository
                .findByStudySubjectIdAndStudyEventDefinitionIdAndSampleOrdinal(ssbid, sedid, ord);
        if (results.isEmpty()) {
            return new StudyEventBean();
        }
        return toBean(results.get(0));
    }

    @Override
    public ArrayList findAllByDefinitionAndSubject(StudyEventDefinitionBean definition, StudySubjectBean subject) {
        return toBeans(studyEventRepository
                .findByStudyEventDefinitionIdAndStudySubjectId(definition.getId(), subject.getId()));
    }

    @Override
    public ArrayList findAllByDefinitionAndSubjectOrderByOrdinal(StudyEventDefinitionBean definition, StudySubjectBean subject) {
        return toBeans(studyEventRepository
                .findByStudyEventDefinitionIdAndStudySubjectIdOrderBySampleOrdinal(definition.getId(), subject.getId()));
    }

    @Override
    public ArrayList findAllByStudyAndStudySubjectId(StudyBean study, int studySubjectId) {
        return toBeans(studyEventRepository.findByStudySubjectId(studySubjectId));
    }

    @Override
    public ArrayList findAllByStudyAndEventDefinitionId(StudyBean study, int eventDefinitionId) {
        return toBeans(studyEventRepository.findByStudyEventDefinitionId(eventDefinitionId));
    }

    @Override
    public int getMaxSampleOrdinal(StudyEventDefinitionBean sedb, StudySubjectBean studySubject) {
        Optional<StudyEventEntity> result = studyEventRepository
                .findTopByStudyEventDefinitionIdAndStudySubjectIdOrderBySampleOrdinalDesc(
                        sedb.getId(), studySubject.getId());
        return result.map(e -> valueOrZero(e.getSampleOrdinal())).orElse(0);
    }

    @Override
    public AuditableEntityBean findByPKAndStudy(int id, StudyBean study) {
        return (AuditableEntityBean) findByPK(id);
    }

    @Override
    public ArrayList findAllByStudy(StudyBean study) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllBySubjectAndStudy(int subjectId, int studyId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllBySubjectId(int subjectId) {
        return toBeans(studyEventRepository.findByStudySubjectId(subjectId));
    }

    @Override
    public ArrayList findAllBySubjectIdOrdered(int subjectId) {
        return toBeans(studyEventRepository.findByStudySubjectIdOrderByDateStart(subjectId));
    }

    @Override
    public HashMap findCRFsByStudy(StudyBean sb) {
        return new HashMap();
    }

    @Override
    public HashMap findCRFsByStudyEvent(StudyEventBean seb) {
        return new HashMap();
    }

    @Override
    public int getDefinitionIdFromStudyEventId(int studyEventId) {
        return studyEventRepository.findById(studyEventId)
                .map(e -> valueOrZero(e.getStudyEventDefinitionId()))
                .orElse(0);
    }

    @Override
    public EntityBean getNextScheduledEvent(String studySubjectOID) {
        return new StudyEventBean();
    }

    @Override
    public ArrayList findAllByStudySubject(StudySubjectBean ssb) {
        return toBeans(studyEventRepository.findByStudySubjectId(ssb.getId()));
    }

    @Override
    public ArrayList findAllByStudySubjectAndDefinition(StudySubjectBean ssb, StudyEventDefinitionBean sed) {
        return new ArrayList();
    }

    @Override
    public Integer countNotRemovedEvents(Integer studyEventDefinitionId) {
        return 0;
    }

    @Override
    public int getCurrentPK() {
        return 0;
    }

    @Override
    public Integer getCountofEventsBasedOnEventStatus(StudyBean currentStudy, SubjectEventStatus subjectEventStatus) {
        return 0;
    }

    @Override
    public Integer getCountofEvents(StudyBean currentStudy) {
        return 0;
    }

    @Override
    public StudyEventBean findAllByStudyEventDefinitionAndCrfOidsAndOrdinal(
            String studyEventDefinitionOid, String crfOrCrfVersionOid, String ordinal, String studySubjectId) {
        return new StudyEventBean();
    }

    @Override
    public HashMap getStudySubjectCRFData(StudyBean sb, int studySubjectId, int eventDefId,
                                          String crfVersionOID, int eventOrdinal) {
        return new HashMap();
    }

    @Override
    public boolean isThisRepeatingEventScheduledMoreThanOneTime(int studyId, int sed_Id) {
        return false;
    }

    // --- Listener interface ---

    private Observer observer;

    @Override
    public void setObserver(Observer o) {
        this.observer = o;
    }

    @Override
    public Observer getObserver() {
        return observer;
    }

    // --- Default method overrides ---

    @Override
    public StudyEvent fetchByStudyEventDefOIDAndOrdinal(String oid, Integer ordinal, int studySubjectId) {
        return null;
    }

    @Override
    public StudyEvent saveOrUpdateTransactional(StudyEventContainer container) {
        return null;
    }

    @Override
    public Integer findMaxOrdinalByStudySubjectStudyEventDefinition(int studySubjectId, int studyEventDefinitionId) {
        return 0;
    }

    // --- Private helpers ---

    private void apply(StudyEventBean bean, StudyEventEntity entity) {
        entity.setStudyEventDefinitionId(bean.getStudyEventDefinitionId());
        entity.setStudySubjectId(bean.getStudySubjectId());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setSubjectEventStatusId(
                bean.getSubjectEventStatus() != null ? bean.getSubjectEventStatus().getId() : SubjectEventStatus.SCHEDULED.getId());
        entity.setLocation(bean.getLocation());
        entity.setSampleOrdinal(bean.getSampleOrdinal());
        entity.setDateStart(toLocalDateTime(bean.getDateStarted()));
        entity.setDateEnd(toLocalDateTime(bean.getDateEnded()));
        entity.setStartTimeFlag(bean.getStartTimeFlag());
        entity.setEndTimeFlag(bean.getEndTimeFlag());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
    }

    private ArrayList toBeans(List<StudyEventEntity> entities) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .sorted(Comparator.comparing(StudyEventEntity::getStudyEventId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private StudyEventBean toBean(StudyEventEntity entity) {
        StudyEventBean bean = new StudyEventBean();
        if (entity.getStudyEventId() != null) {
            bean.setId(entity.getStudyEventId());
        }
        bean.setStudySubjectId(valueOrZero(entity.getStudySubjectId()));
        bean.setStudyEventDefinitionId(valueOrZero(entity.getStudyEventDefinitionId()));
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setSubjectEventStatus(SubjectEventStatus.getFromMap(valueOrZero(entity.getSubjectEventStatusId())));
        bean.setLocation(entity.getLocation() != null ? entity.getLocation() : "");
        bean.setSampleOrdinal(valueOrZero(entity.getSampleOrdinal()));
        bean.setDateStarted(toDate(entity.getDateStart()));
        bean.setDateEnded(toDate(entity.getDateEnd()));
        bean.setStartTimeFlag(entity.getStartTimeFlag() != null && entity.getStartTimeFlag());
        bean.setEndTimeFlag(entity.getEndTimeFlag() != null && entity.getEndTimeFlag());
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
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
