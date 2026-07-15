package org.researchedc.module.recruit.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.recruit.dto.CandidateDTO;
import org.researchedc.module.recruit.dto.ConvertCandidateRequest;
import org.researchedc.module.recruit.dto.ConvertCandidateResultDTO;
import org.researchedc.module.recruit.dto.CreateCandidateRequest;
import org.researchedc.module.recruit.dto.PrescreenResultDTO;
import org.researchedc.module.recruit.dto.RecordPrescreenRequest;
import org.researchedc.module.recruit.entity.CandidateEntity;
import org.researchedc.module.recruit.entity.PrescreenResultEntity;
import org.researchedc.module.recruit.enums.CandidateStatus;
import org.researchedc.module.recruit.enums.EligibilityDecision;
import org.researchedc.module.recruit.repository.CandidateRepository;
import org.researchedc.module.recruit.repository.PrescreenResultRepository;
import org.researchedc.module.subject.dto.CreateSubjectRequest;
import org.researchedc.module.subject.dto.EnrollSubjectRequest;
import org.researchedc.module.subject.dto.SubjectDTO;
import org.researchedc.module.subject.service.SubjectService;
import org.researchedc.app.dto.StudySubjectDTO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class RecruitService {

    private final CandidateRepository candidateRepository;
    private final PrescreenResultRepository prescreenResultRepository;
    private final SubjectService subjectService;
    private final CurrentStudyAccessService currentStudyAccessService;
    private final AuditService auditService;

    public RecruitService(CandidateRepository candidateRepository,
                          PrescreenResultRepository prescreenResultRepository,
                          SubjectService subjectService,
                          CurrentStudyAccessService currentStudyAccessService,
                          AuditService auditService) {
        this.candidateRepository = candidateRepository;
        this.prescreenResultRepository = prescreenResultRepository;
        this.subjectService = subjectService;
        this.currentStudyAccessService = currentStudyAccessService;
        this.auditService = auditService;
    }

    public List<CandidateDTO> listCandidates(Integer studyId, CandidateStatus status, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        List<CandidateEntity> candidates = status == null
                ? candidateRepository.findByStudyIdOrderByCreatedDateDesc(studyId)
                : candidateRepository.findByStudyIdAndStatusOrderByCreatedDateDesc(studyId, status);
        return candidates.stream().map(this::toCandidateDto).toList();
    }

    public CandidateDTO getCandidate(Long id, Integer currentUserId) {
        CandidateEntity candidate = findCandidate(id);
        requireReadAccess(currentUserId, candidate.getStudyId());
        return toCandidateDto(candidate);
    }

    @Transactional
    public CandidateDTO createCandidate(CreateCandidateRequest request, Integer currentUserId) {
        validateCreateRequest(request);
        requireWriteAccess(currentUserId, request.getStudyId());
        String candidateCode = request.getCandidateCode().trim();
        if (candidateRepository.existsByStudyIdAndCandidateCodeIgnoreCase(request.getStudyId(), candidateCode)) {
            throw new IllegalStateException("Candidate code already exists in this study");
        }

        CandidateEntity candidate = new CandidateEntity();
        candidate.setStudyId(request.getStudyId());
        candidate.setCandidateCode(candidateCode);
        candidate.setDisplayName(defaultText(request.getDisplayName()));
        candidate.setContactEmail(defaultText(request.getContactEmail()));
        candidate.setContactPhone(defaultText(request.getContactPhone()));
        candidate.setSource(defaultText(request.getSource()));
        candidate.setNotes(defaultText(request.getNotes()));
        candidate.setStatus(CandidateStatus.NEW);
        candidate.setCreatedBy(currentUserId);
        CandidateEntity saved = candidateRepository.save(candidate);
        record(saved.getStudyId(), AuditEventType.CREATE, "recruit_candidate", saved.getId(),
                saved.getCandidateCode(), currentUserId, "Recruit candidate created");
        return toCandidateDto(saved);
    }

    public List<PrescreenResultDTO> listPrescreenResults(Long candidateId, Integer currentUserId) {
        CandidateEntity candidate = findCandidate(candidateId);
        requireReadAccess(currentUserId, candidate.getStudyId());
        return prescreenResultRepository.findByCandidateIdOrderByReviewedDateDesc(candidateId)
                .stream()
                .map(this::toPrescreenDto)
                .toList();
    }

    @Transactional
    public PrescreenResultDTO recordPrescreen(Long candidateId, RecordPrescreenRequest request,
                                              Integer currentUserId) {
        CandidateEntity candidate = findCandidate(candidateId);
        requireWriteAccess(currentUserId, candidate.getStudyId());
        if (candidate.getStatus() == CandidateStatus.CONVERTED) {
            throw new IllegalStateException("Converted candidates cannot be prescreened again");
        }
        if (request.getDecision() == null) {
            throw new IllegalArgumentException("decision is required");
        }

        PrescreenResultEntity result = new PrescreenResultEntity();
        result.setCandidateId(candidate.getId());
        result.setStudyId(candidate.getStudyId());
        result.setDecision(request.getDecision());
        result.setScore(request.getScore());
        result.setCriteriaSummary(defaultText(request.getCriteriaSummary()));
        result.setReviewNotes(defaultText(request.getReviewNotes()));
        result.setReviewedBy(currentUserId);
        PrescreenResultEntity saved = prescreenResultRepository.save(result);

        candidate.setStatus(statusForDecision(request.getDecision()));
        candidate.setUpdatedDate(LocalDateTime.now());
        candidateRepository.save(candidate);
        record(candidate.getStudyId(), AuditEventType.UPDATE, "recruit_candidate", candidate.getId(),
                candidate.getCandidateCode(), currentUserId, "Prescreen decision: " + request.getDecision());
        return toPrescreenDto(saved);
    }

    @Transactional
    public CandidateDTO rejectCandidate(Long candidateId, String reason, Integer currentUserId) {
        CandidateEntity candidate = findCandidate(candidateId);
        requireWriteAccess(currentUserId, candidate.getStudyId());
        if (candidate.getStatus() == CandidateStatus.CONVERTED) {
            throw new IllegalStateException("Converted candidates cannot be rejected");
        }
        candidate.setStatus(CandidateStatus.REJECTED);
        candidate.setNotes(appendNote(candidate.getNotes(), reason));
        candidate.setUpdatedDate(LocalDateTime.now());
        CandidateEntity saved = candidateRepository.save(candidate);
        record(saved.getStudyId(), AuditEventType.UPDATE, "recruit_candidate", saved.getId(),
                saved.getCandidateCode(), currentUserId, "Recruit candidate rejected");
        return toCandidateDto(saved);
    }

    @Transactional
    public ConvertCandidateResultDTO convertCandidate(Long candidateId, ConvertCandidateRequest request,
                                                      Integer currentUserId) {
        CandidateEntity candidate = findCandidate(candidateId);
        requireWriteAccess(currentUserId, candidate.getStudyId());
        if (candidate.getStatus() != CandidateStatus.ELIGIBLE) {
            throw new IllegalStateException("Only eligible candidates can be converted");
        }

        CreateSubjectRequest subjectRequest = new CreateSubjectRequest();
        subjectRequest.setUniqueIdentifier(resolveSubjectIdentifier(candidate, request));
        subjectRequest.setGender(request.getGender());
        subjectRequest.setDateOfBirth(request.getDateOfBirth());
        subjectRequest.setDobCollected(request.getDateOfBirth() != null);
        SubjectDTO subject = subjectService.createSubject(subjectRequest, currentUserId);

        EnrollSubjectRequest enrollRequest = new EnrollSubjectRequest();
        enrollRequest.setStudyId(candidate.getStudyId());
        enrollRequest.setSubjectId(subject.getSubjectId());
        enrollRequest.setLabel(resolveStudySubjectLabel(candidate, request));
        enrollRequest.setEnrollmentDate(request.getEnrollmentDate() == null
                ? LocalDateTime.now()
                : request.getEnrollmentDate());
        StudySubjectDTO studySubject = subjectService.enrollSubject(enrollRequest, currentUserId);

        candidate.setStatus(CandidateStatus.CONVERTED);
        candidate.setConvertedSubjectId(subject.getSubjectId());
        candidate.setConvertedStudySubjectId(studySubject.getStudySubjectId());
        candidate.setUpdatedDate(LocalDateTime.now());
        CandidateEntity saved = candidateRepository.save(candidate);
        record(saved.getStudyId(), AuditEventType.UPDATE, "recruit_candidate", saved.getId(),
                saved.getCandidateCode(), currentUserId, "Recruit candidate converted to subject");

        ConvertCandidateResultDTO result = new ConvertCandidateResultDTO();
        result.setCandidate(toCandidateDto(saved));
        result.setSubject(subject);
        result.setStudySubject(studySubject);
        return result;
    }

    private CandidateEntity findCandidate(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Recruit candidate not found: " + id));
    }

    private void validateCreateRequest(CreateCandidateRequest request) {
        if (request.getStudyId() == null) {
            throw new IllegalArgumentException("studyId is required");
        }
        if (!StringUtils.hasText(request.getCandidateCode())) {
            throw new IllegalArgumentException("candidateCode is required");
        }
    }

    private CandidateStatus statusForDecision(EligibilityDecision decision) {
        return switch (decision) {
            case ELIGIBLE -> CandidateStatus.ELIGIBLE;
            case INELIGIBLE -> CandidateStatus.INELIGIBLE;
            case NEEDS_REVIEW -> CandidateStatus.PRESCREENED;
        };
    }

    private String resolveSubjectIdentifier(CandidateEntity candidate, ConvertCandidateRequest request) {
        if (StringUtils.hasText(request.getSubjectUniqueIdentifier())) {
            return request.getSubjectUniqueIdentifier().trim();
        }
        return candidate.getCandidateCode();
    }

    private String resolveStudySubjectLabel(CandidateEntity candidate, ConvertCandidateRequest request) {
        if (StringUtils.hasText(request.getStudySubjectLabel())) {
            return request.getStudySubjectLabel().trim();
        }
        return candidate.getCandidateCode();
    }

    private CandidateDTO toCandidateDto(CandidateEntity entity) {
        CandidateDTO dto = new CandidateDTO();
        dto.setId(entity.getId());
        dto.setStudyId(entity.getStudyId());
        dto.setCandidateCode(entity.getCandidateCode());
        dto.setDisplayName(entity.getDisplayName());
        dto.setContactEmail(entity.getContactEmail());
        dto.setContactPhone(entity.getContactPhone());
        dto.setSource(entity.getSource());
        dto.setStatus(entity.getStatus());
        dto.setNotes(entity.getNotes());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());
        dto.setConvertedSubjectId(entity.getConvertedSubjectId());
        dto.setConvertedStudySubjectId(entity.getConvertedStudySubjectId());
        return dto;
    }

    private PrescreenResultDTO toPrescreenDto(PrescreenResultEntity entity) {
        PrescreenResultDTO dto = new PrescreenResultDTO();
        dto.setId(entity.getId());
        dto.setCandidateId(entity.getCandidateId());
        dto.setStudyId(entity.getStudyId());
        dto.setDecision(entity.getDecision());
        dto.setScore(entity.getScore());
        dto.setCriteriaSummary(entity.getCriteriaSummary());
        dto.setReviewNotes(entity.getReviewNotes());
        dto.setReviewedBy(entity.getReviewedBy());
        dto.setReviewedDate(entity.getReviewedDate());
        return dto;
    }

    private String appendNote(String existing, String note) {
        if (!StringUtils.hasText(note)) {
            return existing;
        }
        if (!StringUtils.hasText(existing)) {
            return note.trim();
        }
        return existing + "\n" + note.trim();
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private void requireReadAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canReadStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have read access to this study");
        }
    }

    private void requireWriteAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canWriteStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have write access to this study");
        }
    }

    private void record(Integer studyId, AuditEventType eventType, String entityType, Long entityId,
                        String entityLabel, Integer performedBy, String details) {
        auditService.recordAudit(studyId, eventType, entityType, entityId, entityLabel,
                null, null, performedBy, details, "recruit");
    }
}
