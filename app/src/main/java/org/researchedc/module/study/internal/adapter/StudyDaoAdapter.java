package org.researchedc.module.study.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.module.dataimport.service.ImportStudyLookupPort;
import org.researchedc.module.dataimport.dto.ImportStudy;
import org.researchedc.module.study.entity.StudyEntity;
import org.researchedc.module.study.repository.StudyRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("studyDAO")
@Primary
@Transactional(readOnly = true)
public class StudyDaoAdapter implements ImportStudyLookupPort {

    private final StudyRepository repository;

    public StudyDaoAdapter(StudyRepository repository) {
        this.repository = repository;
    }

    public EntityBean findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(StudyBean::new);
    }

    @Transactional
    public EntityBean create(EntityBean eb) {
        StudyBean bean = (StudyBean) eb;
        StudyEntity entity = new StudyEntity();
        apply(bean, entity);
        entity.setStatusId(Status.AVAILABLE.getId());
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Transactional
    public EntityBean update(EntityBean eb) {
        StudyBean bean = (StudyBean) eb;
        StudyEntity entity = repository.findById(bean.getId()).orElseGet(StudyEntity::new);
        entity.setStudyId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(bean.getUpdaterId());
        return toBean(repository.save(entity));
    }

    public Collection findAll() {
        return toBeans(repository.findAll());
    }

    public Object getEntityFromHashMap(HashMap hm) {
        StudyEntity entity = new StudyEntity();
        entity.setStudyId((Integer) hm.get("study_id"));
        entity.setParentStudyId((Integer) hm.get("parent_study_id"));
        entity.setUniqueIdentifier((String) hm.get("unique_identifier"));
        entity.setSecondaryIdentifier((String) hm.get("secondary_identifier"));
        entity.setName((String) hm.get("name"));
        entity.setSummary((String) hm.get("summary"));
        entity.setDatePlannedStart(toLocalDateTime((Date) hm.get("date_planned_start")));
        entity.setDatePlannedEnd(toLocalDateTime((Date) hm.get("date_planned_end")));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setTypeId((Integer) hm.get("type_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setPrincipalInvestigator((String) hm.get("principal_investigator"));
        entity.setFacilityName((String) hm.get("facility_name"));
        entity.setFacilityCity((String) hm.get("facility_city"));
        entity.setFacilityState((String) hm.get("facility_state"));
        entity.setFacilityZip((String) hm.get("facility_zip"));
        entity.setFacilityCountry((String) hm.get("facility_country"));
        entity.setFacilityRecruitmentStatus((String) hm.get("facility_recruitment_status"));
        entity.setFacilityContactName((String) hm.get("facility_contact_name"));
        entity.setFacilityContactDegree((String) hm.get("facility_contact_degree"));
        entity.setFacilityContactPhone((String) hm.get("facility_contact_phone"));
        entity.setProtocolType((String) hm.get("protocol_type"));
        entity.setProtocolDescription((String) hm.get("protocol_description"));
        entity.setProtocolDateVerification(toLocalDateTime((Date) hm.get("protocol_date_verification")));
        entity.setPhase((String) hm.get("phase"));
        entity.setExpectedTotalEnrollment((Integer) hm.get("expected_total_enrollment"));
        entity.setSponsor((String) hm.get("sponsor"));
        entity.setCollaborators((String) hm.get("collaborators"));
        entity.setMedlineIdentifier((String) hm.get("medline_identifier"));
        entity.setUrl((String) hm.get("url"));
        entity.setUrlDescription((String) hm.get("url_description"));
        entity.setConditions((String) hm.get("conditions"));
        entity.setKeywords((String) hm.get("keywords"));
        entity.setEligibility((String) hm.get("eligibility"));
        entity.setGender((String) hm.get("gender"));
        entity.setAgeMin((String) hm.get("age_min"));
        entity.setAgeMax((String) hm.get("age_max"));
        entity.setHealthyVolunteerAccepted((Boolean) hm.get("healthy_volunteer_accepted"));
        entity.setPurpose((String) hm.get("purpose"));
        entity.setAllocation((String) hm.get("allocation"));
        entity.setMasking((String) hm.get("masking"));
        entity.setControl((String) hm.get("control"));
        entity.setAssignment((String) hm.get("assignment"));
        entity.setEndpoint((String) hm.get("endpoint"));
        entity.setInterventions((String) hm.get("interventions"));
        entity.setDuration((String) hm.get("duration"));
        entity.setSelection((String) hm.get("selection"));
        entity.setTiming((String) hm.get("timing"));
        entity.setOfficialTitle((String) hm.get("official_title"));
        entity.setResultsReference((Boolean) hm.get("results_reference"));
        entity.setOcOid((String) hm.get("oc_oid"));
        return toBean(entity);
    }

    public Collection findAllByUser(String username) {
        return toBeans(repository.findByUserName(username));
    }

    public Collection findAllByUserNotRemoved(String username) {
        return toBeans(repository.findByUserNameNotRemoved(username));
    }

    public ArrayList findAllByStatus(Status status) {
        if (status == null) {
            return new ArrayList();
        }
        return toBeans(repository.findByStatusIdOrderByName(status.getId()));
    }

    public Collection findAllParents() {
        return toBeans(repository.findByParentStudyIdIsNullOrderByName());
    }

    public boolean isAParent(int studyId) {
        return !repository.findByParentStudyIdOrderByName(studyId).isEmpty();
    }

    public Collection findAllByParent(int parentStudyId) {
        return toBeans(repository.findByParentStudyIdOrderByName(parentStudyId));
    }

    public Collection findAllByParentAndLimit(int parentStudyId, boolean isLimited) {
        return toBeans(repository.findByParentStudyIdOrderByName(parentStudyId));
    }

    public Collection findAll(int studyId) {
        return repository.findById(studyId)
                .map(e -> {
                    ArrayList<StudyBean> list = new ArrayList<>();
                    list.add(toBean(e));
                    return (Collection) list;
                })
                .orElseGet(ArrayList::new);
    }

    public Collection<Integer> findAllSiteIdsByStudy(StudyBean study) {
        if (study == null || study.getId() <= 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(repository.findSiteIdsByParentStudyId(study.getId()));
    }

    public Collection<Integer> findOlnySiteIdsByStudy(StudyBean study) {
        if (study == null || study.getId() <= 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(repository.findSiteIdsByParentStudyId(study.getId()));
    }

    public Collection findAllByParentStudyIdOrderedByIdAsc(int parentStudyId) {
        return toBeans(repository.findByParentStudyIdOrderByName(parentStudyId));
    }

    public StudyBean findByStudySubjectId(int studySubjectId) {
        return repository.findByStudySubjectId(studySubjectId)
                .map(this::toBean)
                .orElse(null);
    }

    @Transactional
    public StudyBean updateSitesStatus(StudyBean sb) {
        List<StudyEntity> sites = repository.findByParentStudyIdOrderByName(sb.getId());
        for (StudyEntity site : sites) {
            site.setStatusId(sb.getStatus() != null ? sb.getStatus().getId() : site.getStatusId());
            site.setDateUpdated(LocalDateTime.now());
            site.setUpdateId(sb.getUpdaterId());
            repository.save(site);
        }
        return sb;
    }

    @Transactional
    public StudyBean updateStudyStatus(StudyBean sb) {
        StudyEntity entity = repository.findById(sb.getId()).orElse(null);
        if (entity == null) {
            return sb;
        }
        entity.setStatusId(sb.getStatus() != null ? sb.getStatus().getId() : entity.getStatusId());
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(sb.getUpdaterId());
        entity.setOldStatusId(sb.getOldStatus() != null ? sb.getOldStatus().getId() : null);
        repository.save(entity);
        return sb;
    }

    public StudyBean findByOid(String oid) {
        return repository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public ImportStudy findImportStudyByOid(String oid) {
        StudyBean study = findByOid(oid);
        if (study == null) {
            return null;
        }
        return new ImportStudy(study.getId(), study.getParentStudyId(), study.getName());
    }

    public StudyBean findByUniqueIdentifier(String uniqueIdentifier) {
        return repository.findByUniqueIdentifier(uniqueIdentifier)
                .map(this::toBean)
                .orElse(null);
    }

    public StudyBean findSiteByUniqueIdentifier(String parentUniqueIdentifier, String siteUniqueIdentifier) {
        StudyEntity parent = repository.findByUniqueIdentifier(parentUniqueIdentifier).orElse(null);
        if (parent == null) {
            return null;
        }
        return repository.findByParentStudyIdAndUniqueIdentifier(parent.getStudyId(), siteUniqueIdentifier)
                .map(this::toBean)
                .orElse(null);
    }

    public EntityBean findByName(String name) {
        List<StudyEntity> results = repository.findByNameContainingIgnoreCase(name);
        return results.isEmpty() ? new StudyBean() : toBean(results.get(0));
    }

    public ArrayList<Integer> getStudyIdsByCRF(int crfId) {
        return new ArrayList<>(repository.findStudyIdsByCrfId(crfId));
    }

    private void apply(StudyBean bean, StudyEntity entity) {
        entity.setParentStudyId(bean.getParentStudyId() > 0 ? bean.getParentStudyId() : null);
        entity.setUniqueIdentifier(bean.getIdentifier());
        entity.setSecondaryIdentifier(bean.getSecondaryIdentifier());
        entity.setName(bean.getName());
        entity.setSummary(bean.getSummary());
        entity.setDatePlannedStart(toLocalDateTime(bean.getDatePlannedStart()));
        entity.setDatePlannedEnd(toLocalDateTime(bean.getDatePlannedEnd()));
        entity.setTypeId(bean.getTypeId());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setPrincipalInvestigator(bean.getPrincipalInvestigator());
        entity.setFacilityName(bean.getFacilityName());
        entity.setFacilityCity(bean.getFacilityCity());
        entity.setFacilityState(bean.getFacilityState());
        entity.setFacilityZip(bean.getFacilityZip());
        entity.setFacilityCountry(bean.getFacilityCountry());
        entity.setFacilityRecruitmentStatus(bean.getFacilityRecruitmentStatus());
        entity.setFacilityContactName(bean.getFacilityContactName());
        entity.setFacilityContactDegree(bean.getFacilityContactDegree());
        entity.setFacilityContactPhone(bean.getFacilityContactPhone());
        entity.setProtocolType(bean.getProtocolType());
        entity.setProtocolDescription(bean.getProtocolDescription());
        entity.setProtocolDateVerification(toLocalDateTime(bean.getProtocolDateVerification()));
        entity.setPhase(bean.getPhase());
        entity.setExpectedTotalEnrollment(bean.getExpectedTotalEnrollment());
        entity.setSponsor(bean.getSponsor());
        entity.setCollaborators(bean.getCollaborators());
        entity.setMedlineIdentifier(bean.getMedlineIdentifier());
        entity.setUrl(bean.getUrl());
        entity.setUrlDescription(bean.getUrlDescription());
        entity.setConditions(bean.getConditions());
        entity.setKeywords(bean.getKeywords());
        entity.setEligibility(bean.getEligibility());
        entity.setGender(bean.getGender());
        entity.setAgeMin(bean.getAgeMin());
        entity.setAgeMax(bean.getAgeMax());
        entity.setHealthyVolunteerAccepted(bean.getHealthyVolunteerAccepted());
        entity.setPurpose(bean.getPurpose());
        entity.setAllocation(bean.getAllocation());
        entity.setMasking(bean.getMasking());
        entity.setControl(bean.getControl());
        entity.setAssignment(bean.getAssignment());
        entity.setEndpoint(bean.getEndpoint());
        entity.setInterventions(bean.getInterventions());
        entity.setDuration(bean.getDuration());
        entity.setSelection(bean.getSelection());
        entity.setTiming(bean.getTiming());
        entity.setOfficialTitle(bean.getOfficialTitle());
        entity.setResultsReference(bean.isResultsReference());
        entity.setOcOid(bean.getOid());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
    }

    private ArrayList<StudyBean> toBeans(List<StudyEntity> entities) {
        ArrayList<StudyBean> beans = new ArrayList<>();
        entities.stream()
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private StudyBean toBean(StudyEntity entity) {
        StudyBean bean = new StudyBean();
        if (entity.getStudyId() != null) {
            bean.setId(entity.getStudyId());
        }
        bean.setParentStudyId(valueOrZero(entity.getParentStudyId()));
        bean.setIdentifier(entity.getUniqueIdentifier());
        bean.setSecondaryIdentifier(entity.getSecondaryIdentifier());
        bean.setName(entity.getName());
        bean.setSummary(entity.getSummary());
        bean.setDatePlannedStart(toDate(entity.getDatePlannedStart()));
        bean.setDatePlannedEnd(toDate(entity.getDatePlannedEnd()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setTypeId(entity.getTypeId() != null ? entity.getTypeId() : StudyBean.TYPE_NON_GENETIC);
        bean.setPrincipalInvestigator(entity.getPrincipalInvestigator());
        bean.setFacilityName(entity.getFacilityName());
        bean.setFacilityCity(entity.getFacilityCity());
        bean.setFacilityState(entity.getFacilityState());
        bean.setFacilityZip(entity.getFacilityZip());
        bean.setFacilityCountry(entity.getFacilityCountry());
        bean.setFacilityRecruitmentStatus(entity.getFacilityRecruitmentStatus());
        bean.setFacilityContactName(entity.getFacilityContactName());
        bean.setFacilityContactDegree(entity.getFacilityContactDegree());
        bean.setFacilityContactPhone(entity.getFacilityContactPhone());
        bean.setProtocolType(entity.getProtocolType());
        bean.setProtocolDescription(entity.getProtocolDescription());
        bean.setProtocolDateVerification(toDate(entity.getProtocolDateVerification()));
        bean.setPhase(entity.getPhase());
        bean.setExpectedTotalEnrollment(valueOrZero(entity.getExpectedTotalEnrollment()));
        bean.setSponsor(entity.getSponsor());
        bean.setCollaborators(entity.getCollaborators());
        bean.setMedlineIdentifier(entity.getMedlineIdentifier());
        bean.setUrl(entity.getUrl());
        bean.setUrlDescription(entity.getUrlDescription());
        bean.setConditions(entity.getConditions());
        bean.setKeywords(entity.getKeywords());
        bean.setEligibility(entity.getEligibility());
        bean.setGender(entity.getGender());
        bean.setAgeMin(entity.getAgeMin());
        bean.setAgeMax(entity.getAgeMax());
        bean.setHealthyVolunteerAccepted(Boolean.TRUE.equals(entity.getHealthyVolunteerAccepted()));
        bean.setPurpose(entity.getPurpose());
        bean.setAllocation(entity.getAllocation());
        bean.setMasking(entity.getMasking());
        bean.setControl(entity.getControl());
        bean.setAssignment(entity.getAssignment());
        bean.setEndpoint(entity.getEndpoint());
        bean.setInterventions(entity.getInterventions());
        bean.setDuration(entity.getDuration());
        bean.setSelection(entity.getSelection());
        bean.setTiming(entity.getTiming());
        bean.setOfficialTitle(entity.getOfficialTitle());
        bean.setResultsReference(Boolean.TRUE.equals(entity.getResultsReference()));
        bean.setOid(entity.getOcOid());
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
