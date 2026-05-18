package org.akaza.openclinica.module.study.service;

import java.util.List;
import org.akaza.openclinica.module.study.dto.StudyDetailDTO;
import org.akaza.openclinica.module.study.dto.StudySummaryDTO;
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
            .orElseThrow(() -> new java.util.NoSuchElementException(
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
