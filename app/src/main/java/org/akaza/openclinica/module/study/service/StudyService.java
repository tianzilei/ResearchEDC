package org.akaza.openclinica.module.study.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.akaza.openclinica.module.study.dto.CreateStudyRequest;
import org.akaza.openclinica.module.study.dto.StudyDetailDTO;
import org.akaza.openclinica.module.study.dto.StudySummaryDTO;
import org.akaza.openclinica.module.study.dto.UpdateStudyRequest;
import org.akaza.openclinica.module.study.entity.StudyEntity;
import org.akaza.openclinica.module.study.repository.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;

    public StudyService(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    public List<StudySummaryDTO> listStudies() {
        return studyRepository.findByParentStudyIdIsNullOrderByName()
            .stream()
            .map(this::toSummary)
            .toList();
    }

    public List<StudySummaryDTO> listSites(Integer parentStudyId) {
        return studyRepository.findByParentStudyIdOrderByName(parentStudyId)
            .stream()
            .map(this::toSummary)
            .toList();
    }

    public List<StudySummaryDTO> searchByName(String name) {
        return studyRepository.findByNameContainingIgnoreCase(name)
            .stream()
            .map(this::toSummary)
            .toList();
    }

    public StudyDetailDTO getStudy(Integer studyId) {
        StudyEntity entity = studyRepository.findById(studyId)
            .orElseThrow(() -> new NoSuchElementException(
                "Study not found: " + studyId));
        StudyDetailDTO dto = toDetail(entity);

        if (!entity.isSite()) {
            List<StudySummaryDTO> sites = studyRepository
                .findByParentStudyIdOrderByName(studyId)
                .stream()
                .map(this::toSummary)
                .toList();
            dto.setSites(sites);
        }
        return dto;
    }

    @Transactional
    public StudyDetailDTO createStudy(CreateStudyRequest request, Integer ownerId) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Study name is required");
        }
        StudyEntity entity = new StudyEntity();
        applyCreateRequest(entity, request);
        entity.setDateCreated(LocalDateTime.now());
        entity.setOwnerId(ownerId);

        StudyEntity saved = studyRepository.save(entity);

        return toDetail(saved);
    }

    @Transactional
    public StudyDetailDTO updateStudy(Integer studyId, UpdateStudyRequest request, Integer updaterId) {
        StudyEntity entity = studyRepository.findById(studyId)
            .orElseThrow(() -> new NoSuchElementException(
                "Study not found: " + studyId));

        applyUpdateRequest(entity, request);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(updaterId);

        StudyEntity saved = studyRepository.save(entity);

        return toDetail(saved);
    }

    @Transactional
    public void deleteStudy(Integer studyId, Integer userId) {
        StudyEntity entity = studyRepository.findById(studyId)
            .orElseThrow(() -> new NoSuchElementException(
                "Study not found: " + studyId));
        studyRepository.delete(entity);
    }

    @Transactional
    public void updateStudyStatus(Integer studyId, Integer statusId, Integer userId) {
        StudyEntity entity = studyRepository.findById(studyId)
            .orElseThrow(() -> new NoSuchElementException(
                "Study not found: " + studyId));
        entity.setStatusId(statusId);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(userId);
        studyRepository.save(entity);
    }

    private void applyCreateRequest(StudyEntity entity, CreateStudyRequest r) {
        entity.setName(r.getName());
        entity.setUniqueIdentifier(r.getUniqueIdentifier());
        entity.setSecondaryIdentifier(r.getSecondaryIdentifier());
        entity.setSummary(r.getSummary());
        entity.setDatePlannedStart(r.getDatePlannedStart());
        entity.setDatePlannedEnd(r.getDatePlannedEnd());
        entity.setTypeId(r.getTypeId());
        entity.setStatusId(r.getStatusId());
        entity.setPrincipalInvestigator(r.getPrincipalInvestigator());
        entity.setFacilityName(r.getFacilityName());
        entity.setFacilityCity(r.getFacilityCity());
        entity.setFacilityState(r.getFacilityState());
        entity.setFacilityCountry(r.getFacilityCountry());
        entity.setProtocolType(r.getProtocolType());
        entity.setProtocolDescription(r.getProtocolDescription());
        entity.setPhase(r.getPhase());
        entity.setExpectedTotalEnrollment(r.getExpectedTotalEnrollment());
        entity.setSponsor(r.getSponsor());
        entity.setCollaborators(r.getCollaborators());
        entity.setOfficialTitle(r.getOfficialTitle());
        entity.setConditions(r.getConditions());
        entity.setKeywords(r.getKeywords());
        entity.setEligibility(r.getEligibility());
        entity.setGender(r.getGender());
        entity.setPurpose(r.getPurpose());
        entity.setAllocation(r.getAllocation());
        entity.setMasking(r.getMasking());
        entity.setControl(r.getControl());
        entity.setAssignment(r.getAssignment());
        entity.setEndpoint(r.getEndpoint());
        entity.setInterventions(r.getInterventions());
        entity.setDuration(r.getDuration());
        entity.setSelection(r.getSelection());
        entity.setTiming(r.getTiming());
    }

    private void applyUpdateRequest(StudyEntity entity, UpdateStudyRequest r) {
        if (r.getName() != null) entity.setName(r.getName());
        if (r.getUniqueIdentifier() != null) entity.setUniqueIdentifier(r.getUniqueIdentifier());
        if (r.getSecondaryIdentifier() != null) entity.setSecondaryIdentifier(r.getSecondaryIdentifier());
        if (r.getSummary() != null) entity.setSummary(r.getSummary());
        if (r.getDatePlannedStart() != null) entity.setDatePlannedStart(r.getDatePlannedStart());
        if (r.getDatePlannedEnd() != null) entity.setDatePlannedEnd(r.getDatePlannedEnd());
        if (r.getTypeId() != null) entity.setTypeId(r.getTypeId());
        if (r.getStatusId() != null) entity.setStatusId(r.getStatusId());
        if (r.getPrincipalInvestigator() != null) entity.setPrincipalInvestigator(r.getPrincipalInvestigator());
        if (r.getFacilityName() != null) entity.setFacilityName(r.getFacilityName());
        if (r.getFacilityCity() != null) entity.setFacilityCity(r.getFacilityCity());
        if (r.getFacilityState() != null) entity.setFacilityState(r.getFacilityState());
        if (r.getFacilityCountry() != null) entity.setFacilityCountry(r.getFacilityCountry());
        if (r.getProtocolType() != null) entity.setProtocolType(r.getProtocolType());
        if (r.getProtocolDescription() != null) entity.setProtocolDescription(r.getProtocolDescription());
        if (r.getPhase() != null) entity.setPhase(r.getPhase());
        if (r.getExpectedTotalEnrollment() != null) entity.setExpectedTotalEnrollment(r.getExpectedTotalEnrollment());
        if (r.getSponsor() != null) entity.setSponsor(r.getSponsor());
        if (r.getCollaborators() != null) entity.setCollaborators(r.getCollaborators());
        if (r.getOfficialTitle() != null) entity.setOfficialTitle(r.getOfficialTitle());
        if (r.getConditions() != null) entity.setConditions(r.getConditions());
        if (r.getKeywords() != null) entity.setKeywords(r.getKeywords());
        if (r.getEligibility() != null) entity.setEligibility(r.getEligibility());
        if (r.getGender() != null) entity.setGender(r.getGender());
        if (r.getPurpose() != null) entity.setPurpose(r.getPurpose());
        if (r.getAllocation() != null) entity.setAllocation(r.getAllocation());
        if (r.getMasking() != null) entity.setMasking(r.getMasking());
        if (r.getControl() != null) entity.setControl(r.getControl());
        if (r.getAssignment() != null) entity.setAssignment(r.getAssignment());
        if (r.getEndpoint() != null) entity.setEndpoint(r.getEndpoint());
        if (r.getInterventions() != null) entity.setInterventions(r.getInterventions());
        if (r.getDuration() != null) entity.setDuration(r.getDuration());
        if (r.getSelection() != null) entity.setSelection(r.getSelection());
        if (r.getTiming() != null) entity.setTiming(r.getTiming());
    }

    private StudySummaryDTO toSummary(StudyEntity e) {
        StudySummaryDTO dto = new StudySummaryDTO();
        dto.setStudyId(e.getStudyId());
        dto.setParentStudyId(e.getParentStudyId());
        dto.setSite(e.isSite());
        dto.setName(e.getName());
        dto.setUniqueIdentifier(e.getUniqueIdentifier());
        dto.setOcOid(e.getOcOid());
        dto.setPhase(e.getPhase());
        dto.setPrincipalInvestigator(e.getPrincipalInvestigator());
        dto.setSponsor(e.getSponsor());
        dto.setDateCreated(e.getDateCreated());
        dto.setExpectedTotalEnrollment(e.getExpectedTotalEnrollment());
        return dto;
    }

    private StudyDetailDTO toDetail(StudyEntity e) {
        StudyDetailDTO dto = new StudyDetailDTO();
        dto.setStudyId(e.getStudyId());
        dto.setParentStudyId(e.getParentStudyId());
        dto.setSite(e.isSite());
        dto.setName(e.getName());
        dto.setUniqueIdentifier(e.getUniqueIdentifier());
        dto.setSecondaryIdentifier(e.getSecondaryIdentifier());
        dto.setOcOid(e.getOcOid());
        dto.setOfficialTitle(e.getOfficialTitle());
        dto.setSummary(e.getSummary());
        dto.setPhase(e.getPhase());
        dto.setPrincipalInvestigator(e.getPrincipalInvestigator());
        dto.setSponsor(e.getSponsor());
        dto.setCollaborators(e.getCollaborators());
        dto.setTypeId(e.getTypeId());
        dto.setFacilityName(e.getFacilityName());
        dto.setFacilityCity(e.getFacilityCity());
        dto.setFacilityState(e.getFacilityState());
        dto.setFacilityCountry(e.getFacilityCountry());
        dto.setDatePlannedStart(e.getDatePlannedStart());
        dto.setDatePlannedEnd(e.getDatePlannedEnd());
        dto.setDateCreated(e.getDateCreated());
        dto.setDateUpdated(e.getDateUpdated());
        dto.setOwnerId(e.getOwnerId());
        dto.setExpectedTotalEnrollment(e.getExpectedTotalEnrollment());
        dto.setProtocolType(e.getProtocolType());
        dto.setProtocolDescription(e.getProtocolDescription());
        dto.setConditions(e.getConditions());
        dto.setKeywords(e.getKeywords());
        dto.setEligibility(e.getEligibility());
        dto.setGender(e.getGender());
        dto.setPurpose(e.getPurpose());
        dto.setAllocation(e.getAllocation());
        dto.setMasking(e.getMasking());
        return dto;
    }
}
