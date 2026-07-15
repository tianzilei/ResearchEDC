package org.researchedc.module.recruit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.app.dto.StudySubjectDTO;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.recruit.dto.ConvertCandidateRequest;
import org.researchedc.module.recruit.dto.CreateCandidateRequest;
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
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class RecruitServiceTest {

    @Mock private CandidateRepository candidateRepository;
    @Mock private PrescreenResultRepository prescreenResultRepository;
    @Mock private SubjectService subjectService;
    @Mock private CurrentStudyAccessService currentStudyAccessService;
    @Mock private AuditService auditService;

    private RecruitService service;

    @BeforeEach
    void setUp() {
        service = new RecruitService(candidateRepository, prescreenResultRepository, subjectService,
                currentStudyAccessService, auditService);
    }

    @Test
    void createCandidate_whenUnique_savesAndAudits() {
        CreateCandidateRequest request = new CreateCandidateRequest();
        request.setStudyId(10);
        request.setCandidateCode("C-001");
        request.setDisplayName("Pat Candidate");
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(candidateRepository.existsByStudyIdAndCandidateCodeIgnoreCase(10, "C-001")).thenReturn(false);
        when(candidateRepository.save(any(CandidateEntity.class))).thenAnswer(invocation -> {
            CandidateEntity candidate = invocation.getArgument(0);
            candidate.setId(1L);
            candidate.setCreatedDate(LocalDateTime.now());
            return candidate;
        });

        var result = service.createCandidate(request, 42);

        assertEquals(1L, result.getId());
        assertEquals(CandidateStatus.NEW, result.getStatus());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.CREATE), eq("recruit_candidate"),
                eq(1L), eq("C-001"), isNull(), isNull(), eq(42),
                eq("Recruit candidate created"), eq("recruit"));
    }

    @Test
    void createCandidate_whenNoWriteAccess_denies() {
        CreateCandidateRequest request = new CreateCandidateRequest();
        request.setStudyId(10);
        request.setCandidateCode("C-001");
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.createCandidate(request, 42));
        verify(candidateRepository, never()).save(any());
    }

    @Test
    void recordPrescreen_setsEligibilityStatus() {
        CandidateEntity candidate = candidate(CandidateStatus.NEW);
        RecordPrescreenRequest request = new RecordPrescreenRequest();
        request.setDecision(EligibilityDecision.ELIGIBLE);
        request.setScore(92);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(prescreenResultRepository.save(any(PrescreenResultEntity.class))).thenAnswer(invocation -> {
            PrescreenResultEntity result = invocation.getArgument(0);
            result.setId(2L);
            result.setReviewedDate(LocalDateTime.now());
            return result;
        });
        when(candidateRepository.save(candidate)).thenReturn(candidate);

        var result = service.recordPrescreen(1L, request, 42);

        assertEquals(2L, result.getId());
        assertEquals(EligibilityDecision.ELIGIBLE, result.getDecision());
        assertEquals(CandidateStatus.ELIGIBLE, candidate.getStatus());
    }

    @Test
    void convertCandidate_whenNotEligible_throws() {
        CandidateEntity candidate = candidate(CandidateStatus.PRESCREENED);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.convertCandidate(1L, new ConvertCandidateRequest(), 42));
        verifyNoInteractions(subjectService);
    }

    @Test
    void convertCandidate_whenEligible_createsSubjectAndEnrollment() {
        CandidateEntity candidate = candidate(CandidateStatus.ELIGIBLE);
        ConvertCandidateRequest request = new ConvertCandidateRequest();
        request.setSubjectUniqueIdentifier("SUBJ-001");
        request.setStudySubjectLabel("SS-001");
        request.setGender("f");
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(subjectService.createSubject(any(CreateSubjectRequest.class), eq(42))).thenReturn(subject());
        when(subjectService.enrollSubject(any(EnrollSubjectRequest.class), eq(42))).thenReturn(studySubject());
        when(candidateRepository.save(candidate)).thenReturn(candidate);

        var result = service.convertCandidate(1L, request, 42);

        assertEquals(CandidateStatus.CONVERTED, result.getCandidate().getStatus());
        assertEquals(100, result.getSubject().getSubjectId());
        assertEquals(200, result.getStudySubject().getStudySubjectId());
        verify(subjectService).createSubject(argThat(subjectRequest -> {
            assertEquals("SUBJ-001", subjectRequest.getUniqueIdentifier());
            assertEquals("f", subjectRequest.getGender());
            return true;
        }), eq(42));
        verify(subjectService).enrollSubject(argThat(enrollRequest -> {
            assertEquals(10, enrollRequest.getStudyId());
            assertEquals(100, enrollRequest.getSubjectId());
            assertEquals("SS-001", enrollRequest.getLabel());
            return true;
        }), eq(42));
    }

    @Test
    void listCandidates_filtersByStudyAndStatus() {
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(true);
        when(candidateRepository.findByStudyIdAndStatusOrderByCreatedDateDesc(10, CandidateStatus.ELIGIBLE))
                .thenReturn(List.of(candidate(CandidateStatus.ELIGIBLE)));

        var result = service.listCandidates(10, CandidateStatus.ELIGIBLE, 42);

        assertEquals(1, result.size());
        assertEquals(CandidateStatus.ELIGIBLE, result.get(0).getStatus());
    }

    private static CandidateEntity candidate(CandidateStatus status) {
        CandidateEntity candidate = new CandidateEntity();
        candidate.setId(1L);
        candidate.setStudyId(10);
        candidate.setCandidateCode("C-001");
        candidate.setDisplayName("Pat Candidate");
        candidate.setStatus(status);
        candidate.setCreatedDate(LocalDateTime.now());
        return candidate;
    }

    private static SubjectDTO subject() {
        SubjectDTO subject = new SubjectDTO();
        subject.setSubjectId(100);
        subject.setUniqueIdentifier("SUBJ-001");
        return subject;
    }

    private static StudySubjectDTO studySubject() {
        StudySubjectDTO studySubject = new StudySubjectDTO();
        studySubject.setStudySubjectId(200);
        studySubject.setStudyId(10);
        studySubject.setSubjectId(100);
        studySubject.setLabel("SS-001");
        return studySubject;
    }
}
