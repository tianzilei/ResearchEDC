package org.researchedc.module.econsent.service;

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
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.econsent.dto.AssignConsentRequest;
import org.researchedc.module.econsent.dto.CountersignConsentRequest;
import org.researchedc.module.econsent.dto.CreateConsentTemplateRequest;
import org.researchedc.module.econsent.dto.CreateConsentVersionRequest;
import org.researchedc.module.econsent.dto.SignConsentRequest;
import org.researchedc.module.econsent.entity.ConsentAssignment;
import org.researchedc.module.econsent.entity.ConsentTemplate;
import org.researchedc.module.econsent.entity.ConsentVersion;
import org.researchedc.module.econsent.enums.ConsentAssignmentStatus;
import org.researchedc.module.econsent.enums.ConsentVersionStatus;
import org.researchedc.module.econsent.repository.ConsentAssignmentRepository;
import org.researchedc.module.econsent.repository.ConsentTemplateRepository;
import org.researchedc.module.econsent.repository.ConsentVersionRepository;
import org.researchedc.module.participantaccess.dto.IssuedParticipantTokenDTO;
import org.researchedc.module.participantaccess.dto.ParticipantAccessTokenDTO;
import org.researchedc.module.participantaccess.dto.ParticipantAccountDTO;
import org.researchedc.module.participantaccess.dto.ParticipantBootstrapDTO;
import org.researchedc.module.participantaccess.service.ParticipantAccessService;
import org.researchedc.module.task.dto.CreateTaskRequest;
import org.researchedc.module.task.enums.TaskTargetType;
import org.researchedc.module.task.service.TaskService;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class EconsentServiceTest {

    @Mock private ConsentTemplateRepository templateRepository;
    @Mock private ConsentVersionRepository versionRepository;
    @Mock private ConsentAssignmentRepository assignmentRepository;
    @Mock private ParticipantAccessService participantAccessService;
    @Mock private TaskService taskService;
    @Mock private CurrentStudyAccessService currentStudyAccessService;
    @Mock private AuditService auditService;

    private EconsentService service;

    @BeforeEach
    void setUp() {
        service = new EconsentService(templateRepository, versionRepository, assignmentRepository,
                participantAccessService, taskService, currentStudyAccessService, auditService);
    }

    @Test
    void createTemplate_whenValid_savesAndAudits() {
        CreateConsentTemplateRequest request = new CreateConsentTemplateRequest();
        request.setStudyId(10);
        request.setCode("MAIN");
        request.setName("Main Consent");
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(templateRepository.save(any(ConsentTemplate.class))).thenAnswer(invocation -> {
            ConsentTemplate template = invocation.getArgument(0);
            template.setId(1L);
            return template;
        });

        ConsentTemplate result = service.createTemplate(request, 42);

        assertEquals(1L, result.getId());
        assertEquals("MAIN", result.getCode());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.CREATE), eq("consent_template"),
                eq(1L), eq("Main Consent"), isNull(), isNull(), eq(42),
                eq("Consent template created"), eq("econsent"));
    }

    @Test
    void createVersionAndPublish_enforcesStudyAccessAndStatus() {
        CreateConsentVersionRequest request = new CreateConsentVersionRequest();
        request.setVersionLabel("v1");
        request.setBodyText("Consent body");
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template()));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(versionRepository.save(any(ConsentVersion.class))).thenAnswer(invocation -> {
            ConsentVersion version = invocation.getArgument(0);
            version.setId(2L);
            return version;
        });

        ConsentVersion created = service.createVersion(1L, request, 42);
        assertEquals(ConsentVersionStatus.DRAFT, created.getStatus());

        when(versionRepository.findById(2L)).thenReturn(Optional.of(created));
        ConsentVersion published = service.publishVersion(2L, 42);

        assertEquals(ConsentVersionStatus.PUBLISHED, published.getStatus());
        assertNotNull(published.getPublishedDate());
    }

    @Test
    void assignConsent_whenPublished_createsParticipantTokenAssignmentAndTask() {
        AssignConsentRequest request = assignRequest();
        ConsentVersion version = version(ConsentVersionStatus.PUBLISHED);
        when(versionRepository.findById(2L)).thenReturn(Optional.of(version));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(assignmentRepository.findByStudySubjectIdAndConsentVersionIdAndStatusIn(eq(100), eq(2L), any()))
                .thenReturn(List.of());
        when(participantAccessService.findOrCreateAccountForStudySubject(100, 42)).thenReturn(account());
        when(participantAccessService.issueToken(any(), eq(42))).thenReturn(issuedToken());
        when(assignmentRepository.save(any(ConsentAssignment.class))).thenAnswer(invocation -> {
            ConsentAssignment assignment = invocation.getArgument(0);
            if (assignment.getId() == null) {
                assignment.setId(3L);
            }
            return assignment;
        });
        when(taskService.createTaskId(any(CreateTaskRequest.class), eq(42))).thenReturn(4L);

        var result = service.assignConsent(request, 42);

        assertEquals(3L, result.getAssignment().getId());
        assertEquals(4L, result.getAssignment().getTaskInstanceId());
        assertEquals("/participant/access/raw", result.getParticipantEntryUrl());
        verify(participantAccessService).issueToken(argThat(tokenRequest -> {
            assertEquals(5L, tokenRequest.getParticipantAccountId());
            assertEquals("econsent", tokenRequest.getScope());
            return true;
        }), eq(42));
        verify(taskService).createTaskId(argThat(taskRequest -> {
            assertEquals(TaskTargetType.CONSENT_ASSIGNMENT, taskRequest.getTargetType());
            assertEquals(3L, taskRequest.getTargetId());
            return true;
        }), eq(42));
    }

    @Test
    void assignConsent_whenVersionDraft_throws() {
        when(versionRepository.findById(2L)).thenReturn(Optional.of(version(ConsentVersionStatus.DRAFT)));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.assignConsent(assignRequest(), 42));
        verifyNoInteractions(participantAccessService, taskService);
    }

    @Test
    void signParticipantConsent_whenTokenMatches_marksParticipantSigned() {
        ConsentAssignment assignment = assignment(ConsentAssignmentStatus.ASSIGNED);
        SignConsentRequest request = new SignConsentRequest();
        request.setParticipantName("Pat Subject");
        request.setSignature("Pat Subject");
        request.setEvidence("ip=127.0.0.1");
        when(participantAccessService.verifyToken("raw")).thenReturn(bootstrap());
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        ConsentAssignment result = service.signParticipantConsent(3L, "raw", request);

        assertEquals(ConsentAssignmentStatus.PARTICIPANT_SIGNED, result.getStatus());
        assertEquals("Pat Subject", result.getParticipantSignature());
        assertNotNull(result.getParticipantSignedAt());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.UPDATE), eq("consent_assignment"),
                eq(3L), eq("consent:3"), isNull(), isNull(), isNull(),
                eq("Consent signed by participant"), eq("econsent"));
    }

    @Test
    void signParticipantConsent_whenTokenMismatch_denies() {
        ConsentAssignment assignment = assignment(ConsentAssignmentStatus.ASSIGNED);
        ParticipantBootstrapDTO bootstrap = bootstrap();
        bootstrap.setParticipantAccountId(99L);
        when(participantAccessService.verifyToken("raw")).thenReturn(bootstrap);
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignment));

        assertThrows(AccessDeniedException.class,
                () -> service.signParticipantConsent(3L, "raw", new SignConsentRequest()));
    }

    @Test
    void countersign_whenParticipantSigned_generatesArtifactAndCompletesTask() {
        ConsentAssignment assignment = assignment(ConsentAssignmentStatus.PARTICIPANT_SIGNED);
        assignment.setTaskInstanceId(4L);
        assignment.setParticipantName("Pat Subject");
        assignment.setParticipantSignature("Pat Subject");
        assignment.setParticipantSignedAt(LocalDateTime.now().minusHours(1));
        CountersignConsentRequest request = new CountersignConsentRequest();
        request.setCountersignature("Investigator");
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignment));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(versionRepository.findById(2L)).thenReturn(Optional.of(version(ConsentVersionStatus.PUBLISHED)));
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template()));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        ConsentAssignment result = service.countersign(3L, request, 42);

        assertEquals(ConsentAssignmentStatus.COUNTERSIGNED, result.getStatus());
        assertEquals("consent-3.txt", result.getArtifactName());
        assertTrue(result.getArtifactText().contains("ResearchEDC Signed Consent"));
        assertTrue(result.getArtifactText().contains("Investigator"));
        verify(taskService).completeTask(4L, 42);
    }

    @Test
    void artifact_whenGenerated_returnsTextArtifact() {
        ConsentAssignment assignment = assignment(ConsentAssignmentStatus.COUNTERSIGNED);
        assignment.setArtifactName("consent-3.txt");
        assignment.setArtifactText("artifact body");
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignment));
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(true);

        var result = service.artifact(3L, 42);

        assertEquals("consent-3.txt", result.getArtifactName());
        assertEquals("text/plain;charset=UTF-8", result.getContentType());
        assertEquals("artifact body", result.getContent());
    }

    private static AssignConsentRequest assignRequest() {
        AssignConsentRequest request = new AssignConsentRequest();
        request.setStudySubjectId(100);
        request.setConsentVersionId(2L);
        request.setDueAt(LocalDateTime.now().plusDays(7));
        return request;
    }

    private static ConsentTemplate template() {
        ConsentTemplate template = new ConsentTemplate();
        template.setId(1L);
        template.setStudyId(10);
        template.setCode("MAIN");
        template.setName("Main Consent");
        return template;
    }

    private static ConsentVersion version(ConsentVersionStatus status) {
        ConsentVersion version = new ConsentVersion();
        version.setId(2L);
        version.setTemplateId(1L);
        version.setStudyId(10);
        version.setVersionLabel("v1");
        version.setBodyText("Consent body");
        version.setStatus(status);
        return version;
    }

    private static ConsentAssignment assignment(ConsentAssignmentStatus status) {
        ConsentAssignment assignment = new ConsentAssignment();
        assignment.setId(3L);
        assignment.setStudyId(10);
        assignment.setStudySubjectId(100);
        assignment.setConsentVersionId(2L);
        assignment.setParticipantAccountId(5L);
        assignment.setStatus(status);
        return assignment;
    }

    private static ParticipantAccountDTO account() {
        ParticipantAccountDTO account = new ParticipantAccountDTO();
        account.setId(5L);
        account.setStudyId(10);
        account.setStudySubjectId(100);
        return account;
    }

    private static IssuedParticipantTokenDTO issuedToken() {
        ParticipantAccessTokenDTO token = new ParticipantAccessTokenDTO();
        token.setId(6L);
        IssuedParticipantTokenDTO issued = new IssuedParticipantTokenDTO();
        issued.setToken(token);
        issued.setRawToken("raw");
        issued.setEntryUrl("/participant/access/raw");
        return issued;
    }

    private static ParticipantBootstrapDTO bootstrap() {
        ParticipantBootstrapDTO bootstrap = new ParticipantBootstrapDTO();
        bootstrap.setTokenId(6L);
        bootstrap.setParticipantAccountId(5L);
        bootstrap.setStudyId(10);
        bootstrap.setStudySubjectId(100);
        bootstrap.setScope("econsent");
        return bootstrap;
    }
}
