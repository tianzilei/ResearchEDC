package org.researchedc.module.study.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.app.dto.Status;
import org.researchedc.module.dataimport.service.ImportStudyLookupPort;
import org.researchedc.module.dataimport.dto.ImportStudy;
import org.researchedc.app.dto.StudyDto;
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

    public StudyDto findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(StudyDto::new);
    }

    @Transactional
    public StudyDto create(StudyDto dto) {
        StudyEntity entity = new StudyEntity();
        apply(dto, entity);
        entity.setStatusId(Status.AVAILABLE.getId());
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Transactional
    public StudyDto update(StudyDto dto) {
        StudyEntity entity = repository.findById(dto.getId()).orElseGet(StudyEntity::new);
        entity.setStudyId(dto.getId() > 0 ? dto.getId() : null);
        apply(dto, entity);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(dto.getUpdaterId());
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
                    ArrayList<StudyDto> list = new ArrayList<>();
                    list.add(toBean(e));
                    return (Collection) list;
                })
                .orElseGet(ArrayList::new);
    }

    public Collection<Integer> findAllSiteIdsByStudy(StudyDto study) {
        if (study == null || study.getId() <= 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(repository.findSiteIdsByParentStudyId(study.getId()));
    }

    public Collection<Integer> findOlnySiteIdsByStudy(StudyDto study) {
        if (study == null || study.getId() <= 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(repository.findSiteIdsByParentStudyId(study.getId()));
    }

    public Collection findAllByParentStudyIdOrderedByIdAsc(int parentStudyId) {
        return toBeans(repository.findByParentStudyIdOrderByName(parentStudyId));
    }

    public StudyDto findByStudySubjectId(int studySubjectId) {
        return repository.findByStudySubjectId(studySubjectId)
                .map(this::toBean)
                .orElse(null);
    }

    @Transactional
    public StudyDto updateSitesStatus(StudyDto sb) {
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
    public StudyDto updateStudyStatus(StudyDto sb) {
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

    public StudyDto findByOid(String oid) {
        return repository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public ImportStudy findImportStudyByOid(String oid) {
        StudyDto study = findByOid(oid);
        if (study == null) {
            return null;
        }
        return new ImportStudy(study.getId(), study.getParentStudyId(), study.getName());
    }

    public StudyDto findByUniqueIdentifier(String uniqueIdentifier) {
        return repository.findByUniqueIdentifier(uniqueIdentifier)
                .map(this::toBean)
                .orElse(null);
    }

    public StudyDto findSiteByUniqueIdentifier(String parentUniqueIdentifier, String siteUniqueIdentifier) {
        StudyEntity parent = repository.findByUniqueIdentifier(parentUniqueIdentifier).orElse(null);
        if (parent == null) {
            return null;
        }
        return repository.findByParentStudyIdAndUniqueIdentifier(parent.getStudyId(), siteUniqueIdentifier)
                .map(this::toBean)
                .orElse(null);
    }

    public StudyDto findByName(String name) {
        List<StudyEntity> results = repository.findByNameContainingIgnoreCase(name);
        return results.isEmpty() ? new StudyDto() : toBean(results.get(0));
    }

    public ArrayList<Integer> getStudyIdsByCRF(int crfId) {
        return new ArrayList<>(repository.findStudyIdsByCrfId(crfId));
    }

    private void apply(StudyDto dto, StudyEntity entity) {
        entity.setParentStudyId(dto.getParentStudyId() > 0 ? dto.getParentStudyId() : null);
        entity.setUniqueIdentifier(dto.getIdentifier());
        entity.setSecondaryIdentifier(dto.getSecondaryIdentifier());
        entity.setName(dto.getName());
        entity.setSummary(dto.getSummary());
        entity.setDatePlannedStart(toLocalDateTime(dto.getDatePlannedStart()));
        entity.setDatePlannedEnd(toLocalDateTime(dto.getDatePlannedEnd()));
        entity.setTypeId(dto.getTypeId());
        entity.setStatusId(dto.getStatus() != null ? dto.getStatus().getId() : Status.INVALID.getId());
        entity.setPrincipalInvestigator(dto.getPrincipalInvestigator());
        entity.setFacilityName(dto.getFacilityName());
        entity.setFacilityCity(dto.getFacilityCity());
        entity.setFacilityState(dto.getFacilityState());
        entity.setFacilityZip(dto.getFacilityZip());
        entity.setFacilityCountry(dto.getFacilityCountry());
        entity.setFacilityRecruitmentStatus(dto.getFacilityRecruitmentStatus());
        entity.setFacilityContactName(dto.getFacilityContactName());
        entity.setFacilityContactDegree(dto.getFacilityContactDegree());
        entity.setFacilityContactPhone(dto.getFacilityContactPhone());
        entity.setProtocolType(dto.getProtocolType());
        entity.setProtocolDescription(dto.getProtocolDescription());
        entity.setProtocolDateVerification(toLocalDateTime(dto.getProtocolDateVerification()));
        entity.setPhase(dto.getPhase());
        entity.setExpectedTotalEnrollment(dto.getExpectedTotalEnrollment());
        entity.setSponsor(dto.getSponsor());
        entity.setCollaborators(dto.getCollaborators());
        entity.setMedlineIdentifier(dto.getMedlineIdentifier());
        entity.setUrl(dto.getUrl());
        entity.setUrlDescription(dto.getUrlDescription());
        entity.setConditions(dto.getConditions());
        entity.setKeywords(dto.getKeywords());
        entity.setEligibility(dto.getEligibility());
        entity.setGender(dto.getGender());
        entity.setAgeMin(dto.getAgeMin());
        entity.setAgeMax(dto.getAgeMax());
        entity.setHealthyVolunteerAccepted(dto.getHealthyVolunteerAccepted());
        entity.setPurpose(dto.getPurpose());
        entity.setAllocation(dto.getAllocation());
        entity.setMasking(dto.getMasking());
        entity.setControl(dto.getControl());
        entity.setAssignment(dto.getAssignment());
        entity.setEndpoint(dto.getEndpoint());
        entity.setInterventions(dto.getInterventions());
        entity.setDuration(dto.getDuration());
        entity.setSelection(dto.getSelection());
        entity.setTiming(dto.getTiming());
        entity.setOfficialTitle(dto.getOfficialTitle());
        entity.setResultsReference(dto.isResultsReference());
        entity.setOcOid(dto.getOid());
        entity.setOwnerId(dto.getOwnerId());
        entity.setUpdateId(dto.getUpdaterId());
    }

    private ArrayList<StudyDto> toBeans(List<StudyEntity> entities) {
        ArrayList<StudyDto> dtos = new ArrayList<>();
        entities.stream()
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    private StudyDto toBean(StudyEntity entity) {
        StudyDto dto = new StudyDto();
        if (entity.getStudyId() != null) {
            dto.setId(entity.getStudyId());
        }
        dto.setParentStudyId(valueOrZero(entity.getParentStudyId()));
        dto.setIdentifier(entity.getUniqueIdentifier());
        dto.setSecondaryIdentifier(entity.getSecondaryIdentifier());
        dto.setName(entity.getName());
        dto.setSummary(entity.getSummary());
        dto.setDatePlannedStart(toDate(entity.getDatePlannedStart()));
        dto.setDatePlannedEnd(toDate(entity.getDatePlannedEnd()));
        dto.setOwnerId(valueOrZero(entity.getOwnerId()));
        dto.setUpdaterId(valueOrZero(entity.getUpdateId()));
        dto.setCreatedDate(toDate(entity.getDateCreated()));
        dto.setUpdatedDate(toDate(entity.getDateUpdated()));
        dto.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        dto.setTypeId(entity.getTypeId() != null ? entity.getTypeId() : StudyDto.TYPE_NON_GENETIC);
        dto.setPrincipalInvestigator(entity.getPrincipalInvestigator());
        dto.setFacilityName(entity.getFacilityName());
        dto.setFacilityCity(entity.getFacilityCity());
        dto.setFacilityState(entity.getFacilityState());
        dto.setFacilityZip(entity.getFacilityZip());
        dto.setFacilityCountry(entity.getFacilityCountry());
        dto.setFacilityRecruitmentStatus(entity.getFacilityRecruitmentStatus());
        dto.setFacilityContactName(entity.getFacilityContactName());
        dto.setFacilityContactDegree(entity.getFacilityContactDegree());
        dto.setFacilityContactPhone(entity.getFacilityContactPhone());
        dto.setProtocolType(entity.getProtocolType());
        dto.setProtocolDescription(entity.getProtocolDescription());
        dto.setProtocolDateVerification(toDate(entity.getProtocolDateVerification()));
        dto.setPhase(entity.getPhase());
        dto.setExpectedTotalEnrollment(valueOrZero(entity.getExpectedTotalEnrollment()));
        dto.setSponsor(entity.getSponsor());
        dto.setCollaborators(entity.getCollaborators());
        dto.setMedlineIdentifier(entity.getMedlineIdentifier());
        dto.setUrl(entity.getUrl());
        dto.setUrlDescription(entity.getUrlDescription());
        dto.setConditions(entity.getConditions());
        dto.setKeywords(entity.getKeywords());
        dto.setEligibility(entity.getEligibility());
        dto.setGender(entity.getGender());
        dto.setAgeMin(entity.getAgeMin());
        dto.setAgeMax(entity.getAgeMax());
        dto.setHealthyVolunteerAccepted(Boolean.TRUE.equals(entity.getHealthyVolunteerAccepted()));
        dto.setPurpose(entity.getPurpose());
        dto.setAllocation(entity.getAllocation());
        dto.setMasking(entity.getMasking());
        dto.setControl(entity.getControl());
        dto.setAssignment(entity.getAssignment());
        dto.setEndpoint(entity.getEndpoint());
        dto.setInterventions(entity.getInterventions());
        dto.setDuration(entity.getDuration());
        dto.setSelection(entity.getSelection());
        dto.setTiming(entity.getTiming());
        dto.setOfficialTitle(entity.getOfficialTitle());
        dto.setResultsReference(Boolean.TRUE.equals(entity.getResultsReference()));
        dto.setOid(entity.getOcOid());
        return dto;
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
