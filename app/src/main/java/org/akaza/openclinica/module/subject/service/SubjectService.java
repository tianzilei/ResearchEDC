package org.akaza.openclinica.module.subject.service;

import java.util.List;
import org.akaza.openclinica.module.subject.dto.StudySubjectDTO;
import org.akaza.openclinica.module.subject.dto.SubjectDTO;
import org.akaza.openclinica.module.subject.entity.StudySubjectEntity;
import org.akaza.openclinica.module.subject.entity.SubjectEntity;
import org.akaza.openclinica.module.subject.repository.StudySubjectRepository;
import org.akaza.openclinica.module.subject.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final StudySubjectRepository studySubjectRepository;

    public SubjectService(SubjectRepository subjectRepository,
                          StudySubjectRepository studySubjectRepository) {
        this.subjectRepository = subjectRepository;
        this.studySubjectRepository = studySubjectRepository;
    }

    public List<SubjectDTO> searchSubjects(String query) {
        return subjectRepository.findByUniqueIdentifierContainingIgnoreCase(query)
            .stream()
            .map(this::toSubjectDto)
            .toList();
    }

    public SubjectDTO getSubject(Integer subjectId) {
        SubjectEntity entity = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "Subject not found: " + subjectId));
        return toSubjectDto(entity);
    }

    public List<StudySubjectDTO> listStudySubjects(Integer studyId) {
        return studySubjectRepository.findByStudyIdOrderByLabel(studyId)
            .stream()
            .map(this::toStudySubjectDto)
            .toList();
    }

    public StudySubjectDTO getStudySubject(Integer studySubjectId) {
        StudySubjectEntity entity = studySubjectRepository.findById(studySubjectId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "StudySubject not found: " + studySubjectId));
        return toStudySubjectDto(entity);
    }

    private SubjectDTO toSubjectDto(SubjectEntity e) {
        SubjectDTO dto = new SubjectDTO();
        dto.setSubjectId(e.getSubjectId());
        dto.setUniqueIdentifier(e.getUniqueIdentifier());
        dto.setDateOfBirth(e.getDateOfBirth());
        dto.setGender(e.getGender());
        dto.setDobCollected(e.getDobCollected());
        dto.setDateCreated(e.getDateCreated());
        return dto;
    }

    private StudySubjectDTO toStudySubjectDto(StudySubjectEntity e) {
        StudySubjectDTO dto = new StudySubjectDTO();
        dto.setStudySubjectId(e.getStudySubjectId());
        dto.setStudyId(e.getStudyId());
        dto.setSubjectId(e.getSubjectId());
        dto.setLabel(e.getLabel());
        dto.setSecondaryLabel(e.getSecondaryLabel());
        dto.setOcOid(e.getOcOid());
        dto.setEnrollmentDate(e.getEnrollmentDate());
        dto.setDateCreated(e.getDateCreated());
        dto.setDateUpdated(e.getDateUpdated());
        return dto;
    }
}
