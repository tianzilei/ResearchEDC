package org.researchedc.module.subject.service;

import java.time.LocalDateTime;
import java.util.List;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.event.dto.ScheduleEventRequest;
import org.researchedc.module.event.service.EventService;
import org.researchedc.module.subject.dto.CreateSubjectRequest;
import org.researchedc.module.subject.dto.EnrollSubjectRequest;
import org.researchedc.module.subject.dto.StudySubjectDTO;
import org.researchedc.module.subject.dto.SubjectDTO;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.entity.SubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.researchedc.module.subject.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final StudySubjectRepository studySubjectRepository;
    private final EventService eventService;
    private final AuditService auditService;

    public SubjectService(SubjectRepository subjectRepository,
                          StudySubjectRepository studySubjectRepository,
                          EventService eventService,
                          AuditService auditService) {
        this.subjectRepository = subjectRepository;
        this.studySubjectRepository = studySubjectRepository;
        this.eventService = eventService;
        this.auditService = auditService;
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

    @Transactional
    public SubjectDTO createSubject(CreateSubjectRequest request, Integer ownerId) {
        if (request.getUniqueIdentifier() == null || request.getUniqueIdentifier().isBlank()) {
            throw new IllegalArgumentException("Subject uniqueIdentifier is required");
        }
        SubjectEntity entity = new SubjectEntity();
        entity.setUniqueIdentifier(request.getUniqueIdentifier());
        entity.setDateOfBirth(request.getDateOfBirth());
        entity.setGender(request.getGender());
        entity.setDobCollected(request.getDobCollected());
        entity.setDateCreated(LocalDateTime.now());
        entity.setOwnerId(ownerId);

        SubjectEntity saved = subjectRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.CREATE, "Subject",
                saved.getSubjectId().longValue(), saved.getUniqueIdentifier(),
                null, null, ownerId, null, "subject");

        return toSubjectDto(saved);
    }

    @Transactional
    public StudySubjectDTO enrollSubject(EnrollSubjectRequest request, Integer ownerId) {
        if (request.getStudyId() == null) {
            throw new IllegalArgumentException("Study ID is required");
        }
        if (request.getSubjectId() == null) {
            throw new IllegalArgumentException("Subject ID is required");
        }
        if (!subjectRepository.existsById(request.getSubjectId())) {
            throw new java.util.NoSuchElementException(
                "Subject not found: " + request.getSubjectId());
        }

        StudySubjectEntity entity = new StudySubjectEntity();
        entity.setStudyId(request.getStudyId());
        entity.setSubjectId(request.getSubjectId());
        entity.setLabel(request.getLabel());
        entity.setSecondaryLabel(request.getSecondaryLabel());
        entity.setEnrollmentDate(request.getEnrollmentDate());
        entity.setOcOid(request.getOcOid() != null ? request.getOcOid()
                : "SS_" + request.getSubjectId() + "_" + request.getStudyId());
        entity.setDateCreated(LocalDateTime.now());
        entity.setOwnerId(ownerId);

        StudySubjectEntity saved = studySubjectRepository.save(entity);

        auditService.recordAudit(
                request.getStudyId(), AuditEventType.CREATE, "StudySubject",
                saved.getStudySubjectId().longValue(), saved.getLabel(),
                null, null, ownerId, "Enrolled in study " + request.getStudyId(), "subject");

        // Optionally create a study event after enrollment
        if (request.getEventDefinitionId() != null && request.getEventDefinitionId() > 0) {
            ScheduleEventRequest eventRequest = new ScheduleEventRequest();
            eventRequest.setStudySubjectId(saved.getStudySubjectId());
            eventRequest.setStudyEventDefinitionId(request.getEventDefinitionId());
            eventRequest.setLocation(request.getEventLocation());
            eventRequest.setStartDate(request.getEventStartDate());
            eventRequest.setEndDate(request.getEventStartDate());
            eventRequest.setStatusId(1);
            eventRequest.setSubjectEventStatusId(1);
            eventService.scheduleEvent(eventRequest, ownerId);
        }

        return toStudySubjectDto(saved);
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
