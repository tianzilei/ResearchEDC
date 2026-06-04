package org.researchedc.module.event.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.domain.SourceDataVerification;
import org.researchedc.module.event.entity.EventDefinitionCrfEntity;
import org.researchedc.module.event.repository.EventDefinitionCrfRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("eventDefinitionCRFDAO")
@Primary
@Transactional(readOnly = true)
public class EventDefinitionCrfDaoAdapter implements EventDefinitionCRFDao {

    private final EventDefinitionCrfRepository repository;

    public EventDefinitionCrfDaoAdapter(EventDefinitionCrfRepository repository) {
        this.repository = repository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(EventDefinitionCRFBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        EventDefinitionCRFBean bean = (EventDefinitionCRFBean) eb;
        EventDefinitionCrfEntity entity = new EventDefinitionCrfEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        EventDefinitionCRFBean bean = (EventDefinitionCRFBean) eb;
        EventDefinitionCrfEntity entity = repository.findById(bean.getId())
                .orElseGet(EventDefinitionCrfEntity::new);
        entity.setEventDefinitionCrfId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Override
    public Collection findAll() {
        return toBeans(repository.findAll());
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
        EventDefinitionCrfEntity entity = new EventDefinitionCrfEntity();
        entity.setEventDefinitionCrfId((Integer) hm.get("event_definition_crf_id"));
        entity.setStudyEventDefinitionId((Integer) hm.get("study_event_definition_id"));
        entity.setStudyId((Integer) hm.get("study_id"));
        entity.setCrfId((Integer) hm.get("crf_id"));
        entity.setRequiredCrf((Boolean) hm.get("required_crf"));
        entity.setDoubleEntry((Boolean) hm.get("double_entry"));
        entity.setRequireAllTextFilled((Boolean) hm.get("require_all_text_filled"));
        entity.setDecisionConditions((Boolean) hm.get("decision_conditions"));
        entity.setNullValues((String) hm.get("null_values"));
        entity.setDefaultVersionId((Integer) hm.get("default_version_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setOrdinal((Integer) hm.get("ordinal"));
        entity.setElectronicSignature((Boolean) hm.get("electronic_signature"));
        entity.setHideCrf((Boolean) hm.get("hide_crf"));
        entity.setSourceDataVerificationCode((Integer) hm.get("source_data_verification_code"));
        entity.setSelectedVersionIds((String) hm.get("selected_version_ids"));
        entity.setParentId((Integer) hm.get("parent_id"));
        entity.setParticipantForm((Boolean) hm.get("participant_form"));
        entity.setAllowAnonymousSubmission((Boolean) hm.get("allow_anonymous_submission"));
        entity.setSubmissionUrl((String) hm.get("submission_url"));
        return toBean(entity);
    }

    @Override
    public Collection findAllByDefinition(int definitionId) {
        return toBeans(repository.findByStudyEventDefinitionId(definitionId));
    }

    @Override
    public Collection findAllByDefinition(StudyBean study, int definitionId) {
        return toBeans(repository.findByStudyEventDefinitionIdAndStudyId(definitionId, study.getId()));
    }

    @Override
    public Collection findAllByCRF(int crfId) {
        return toBeans(repository.findByCrfId(crfId));
    }

    @Override
    public ArrayList findByDefaultVersion(int versionId) {
        return toBeans(repository.findByDefaultVersionId(versionId));
    }

    @Override
    public ArrayList findAllByCrfDefinitionInSiteOnly(int definitionId, int crfId) {
        return new ArrayList();
    }

    @Override
    public Collection findAllActiveParentsByEventDefinitionId(int definitionId) {
        return new ArrayList();
    }

    @Override
    public Collection findAllActiveNonHiddenByEventDefinitionIdAndStudy(int definitionId, StudyBean study) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllByEventDefinitionId(int eventDefinitionId) {
        return toBeans(repository.findByStudyEventDefinitionId(eventDefinitionId));
    }

    @Override
    public ArrayList findAllByEventDefinitionIdAndOrdinal(int eventDefinitionId, int ordinal) {
        return toBeans(repository.findByStudyEventDefinitionIdAndOrdinal(eventDefinitionId, ordinal));
    }

    @Override
    public ArrayList findAllActiveByEventDefinitionId(int eventDefinitionId) {
        return toBeans(repository.findByStudyEventDefinitionIdAndStatusId(
                eventDefinitionId, Status.AVAILABLE.getId()));
    }

    @Override
    public Collection findAllActiveByEventDefinitionId(StudyBean study, int eventDefinitionId) {
        return toBeans(repository.findByStudyEventDefinitionIdAndStatusIdAndStudyId(
                eventDefinitionId, Status.AVAILABLE.getId(), study.getId()));
    }

    @Override
    public Collection findAllByEventDefinitionId(StudyBean study, int eventDefinitionId) {
        return toBeans(repository.findByStudyEventDefinitionIdAndStudyId(eventDefinitionId, study.getId()));
    }

    @Override
    public boolean isRequiredInDefinition(int crfVersionId, StudyEventBean studyEvent) {
        return false;
    }

    @Override
    public EventDefinitionCRFBean findByStudyEventIdAndCRFVersionId(StudyBean study, int studyEventId, int crfVersionId) {
        return new EventDefinitionCRFBean();
    }

    @Override
    public EventDefinitionCRFBean findByStudyEventDefinitionIdAndCRFId(int studyEventDefinitionId, int crfId) {
        return repository.findByStudyEventDefinitionIdAndCrfId(studyEventDefinitionId, crfId)
                .map(this::toBean)
                .orElseGet(EventDefinitionCRFBean::new);
    }

    @Override
    public EventDefinitionCRFBean findByStudyEventDefinitionIdAndCRFIdAndStudyId(
            int studyEventDefinitionId, int crfId, int studyId) {
        return repository.findByStudyEventDefinitionIdAndCrfIdAndStudyId(studyEventDefinitionId, crfId, studyId)
                .map(this::toBean)
                .orElseGet(EventDefinitionCRFBean::new);
    }

    @Override
    public EventDefinitionCRFBean findByStudyEventDefinitionIdAndCRFId(
            StudyBean study, int studyEventDefinitionId, int crfId) {
        return repository.findByStudyEventDefinitionIdAndCrfIdAndStudyId(
                        studyEventDefinitionId, crfId, study.getId())
                .map(this::toBean)
                .orElseGet(EventDefinitionCRFBean::new);
    }

    @Override
    public EventDefinitionCRFBean findForStudyByStudyEventDefinitionIdAndCRFId(
            int studyEventDefinitionId, int crfId) {
        return repository.findByStudyEventDefinitionIdAndCrfId(studyEventDefinitionId, crfId)
                .map(this::toBean)
                .orElseGet(EventDefinitionCRFBean::new);
    }

    @Override
    public EventDefinitionCRFBean findForStudyByStudyEventIdAndCRFVersionId(int studyEventId, int crfVersionId) {
        return new EventDefinitionCRFBean();
    }

    @Override
    public ArrayList findAllDefIdandStudyId(Integer studyEventDefnId, Integer studyId) {
        return new ArrayList();
    }

    @Override
    public Set<String> findHiddenCrfIdsBySite(StudyBean study) {
        return new HashSet<>();
    }

    @Override
    public Set<String> findHiddenCrfNamesBySite(StudyBean study) {
        return new HashSet<>();
    }

    @Override
    public Map<Integer, SortedSet<EventDefinitionCRFBean>> buildEventDefinitionCRFListByStudyEventDefinition(
            Integer studySubjectId, Integer siteId, Integer parentStudyId) {
        return new HashMap<>();
    }

    @Override
    public Map<Integer, SortedSet<EventDefinitionCRFBean>> buildEventDefinitionCRFListByStudyEventDefinitionForStudy(
            Integer studySubjectId) {
        return new HashMap<>();
    }

    @Override
    public Collection findAllParentsByDefinition(int definitionId) {
        return toBeans(repository.findActiveParentsByEventDefinitionId(definitionId));
    }

    @Override
    public Collection findAllByDefinitionAndSiteIdAndParentStudyId(int definitionId, int siteId, int parentStudyId) {
        return toBeans(repository.findByDefinitionAndSiteIdAndParentStudyId(definitionId, siteId, parentStudyId));
    }

    @Override
    public ArrayList<EventDefinitionCRFBean> findAllActiveSitesAndStudiesPerParentStudy(int parentStudyId) {
        List<EventDefinitionCrfEntity> entities = repository.findAll().stream()
                .filter(e -> e.getStudyId() != null && e.getStudyId().equals(parentStudyId))
                .filter(e -> e.getStatusId() != null && e.getStatusId().equals(Status.AVAILABLE.getId()))
                .collect(Collectors.toList());
        return toBeans(entities);
    }

    @Override
    public ArrayList<EventDefinitionCRFBean> findAllSubmissionUriAndStudyId(String submissionUri, int studyId) {
        return toBeans(repository.findBySubmissionUrlAndStudyId(submissionUri, studyId));
    }

    @Override
    public List findAllCrfMigrationDoesNotPerform(CRFVersionBean sourceCrfVersionBean, CRFVersionBean targetCrfVersionBean,
                                                   ArrayList<String> studyEventDefnlist, ArrayList<String> sitelist) {
        return new ArrayList();
    }

    private void apply(EventDefinitionCRFBean bean, EventDefinitionCrfEntity entity) {
        entity.setStudyEventDefinitionId(bean.getStudyEventDefinitionId() > 0 ? bean.getStudyEventDefinitionId() : null);
        entity.setStudyId(bean.getStudyId() > 0 ? bean.getStudyId() : null);
        entity.setCrfId(bean.getCrfId() > 0 ? bean.getCrfId() : null);
        entity.setRequiredCrf(bean.isRequiredCRF());
        entity.setDoubleEntry(bean.isDoubleEntry());
        entity.setRequireAllTextFilled(bean.isRequireAllTextFilled());
        entity.setDecisionConditions(bean.isDecisionCondition());
        entity.setNullValues(bean.getNullValues());
        entity.setDefaultVersionId(bean.getDefaultVersionId() > 0 ? bean.getDefaultVersionId() : null);
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(bean.getOwnerId() > 0 ? bean.getOwnerId() : null);
        entity.setUpdateId(bean.getUpdaterId() > 0 ? bean.getUpdaterId() : null);
        entity.setOrdinal(bean.getOrdinal() > 0 ? bean.getOrdinal() : null);
        entity.setElectronicSignature(bean.isElectronicSignature());
        entity.setHideCrf(bean.isHideCrf());
        entity.setSourceDataVerificationCode(
                bean.getSourceDataVerification() != null ? bean.getSourceDataVerification().getCode() : null);
        entity.setSelectedVersionIds(bean.getSelectedVersionIds());
        entity.setParentId(bean.getParentId() > 0 ? bean.getParentId() : null);
        entity.setParticipantForm(bean.isParticipantForm());
        entity.setAllowAnonymousSubmission(bean.isAllowAnonymousSubmission());
        entity.setSubmissionUrl(bean.getSubmissionUrl());
    }

    private ArrayList toBeans(List<EventDefinitionCrfEntity> entities) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .sorted(Comparator.comparing(EventDefinitionCrfEntity::getEventDefinitionCrfId,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private EventDefinitionCRFBean toBean(EventDefinitionCrfEntity entity) {
        EventDefinitionCRFBean bean = new EventDefinitionCRFBean();
        if (entity.getEventDefinitionCrfId() != null) {
            bean.setId(entity.getEventDefinitionCrfId());
        }
        bean.setStudyEventDefinitionId(valueOrZero(entity.getStudyEventDefinitionId()));
        bean.setStudyId(valueOrZero(entity.getStudyId()));
        bean.setCrfId(valueOrZero(entity.getCrfId()));
        bean.setRequiredCRF(entity.getRequiredCrf() != null && entity.getRequiredCrf());
        bean.setDoubleEntry(entity.getDoubleEntry() != null && entity.getDoubleEntry());
        bean.setRequireAllTextFilled(entity.getRequireAllTextFilled() != null && entity.getRequireAllTextFilled());
        bean.setDecisionCondition(entity.getDecisionConditions() != null && entity.getDecisionConditions());
        bean.setNullValues(entity.getNullValues() != null ? entity.getNullValues() : "");
        bean.setDefaultVersionId(valueOrZero(entity.getDefaultVersionId()));
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setOrdinal(valueOrZero(entity.getOrdinal()));
        bean.setElectronicSignature(entity.getElectronicSignature() != null && entity.getElectronicSignature());
        bean.setHideCrf(entity.getHideCrf() != null && entity.getHideCrf());
        bean.setSourceDataVerification(
                entity.getSourceDataVerificationCode() != null
                        ? SourceDataVerification.getByCode(entity.getSourceDataVerificationCode())
                        : null);
        bean.setSelectedVersionIds(entity.getSelectedVersionIds() != null ? entity.getSelectedVersionIds() : "");
        bean.setParentId(valueOrZero(entity.getParentId()));
        bean.setParticipantForm(entity.getParticipantForm() != null && entity.getParticipantForm());
        bean.setAllowAnonymousSubmission(entity.getAllowAnonymousSubmission() != null && entity.getAllowAnonymousSubmission());
        bean.setSubmissionUrl(entity.getSubmissionUrl() != null ? entity.getSubmissionUrl() : "");
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
