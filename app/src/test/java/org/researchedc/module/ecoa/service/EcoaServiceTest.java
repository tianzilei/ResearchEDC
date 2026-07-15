package org.researchedc.module.ecoa.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.ecoa.dto.CreateEcoaScheduleRequest;
import org.researchedc.module.ecoa.dto.RecordEcoaCompletionRequest;
import org.researchedc.module.ecoa.entity.EcoaAssignment;
import org.researchedc.module.ecoa.entity.EcoaSchedule;
import org.researchedc.module.ecoa.enums.EcoaAssignmentStatus;
import org.researchedc.module.ecoa.repository.EcoaAssignmentRepository;
import org.researchedc.module.ecoa.repository.EcoaScheduleRepository;
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
class EcoaServiceTest {

    @Mock private EcoaScheduleRepository scheduleRepository;
    @Mock private EcoaAssignmentRepository assignmentRepository;
    @Mock private ParticipantAccessService participantAccessService;
    @Mock private TaskService taskService;
    @Mock private CurrentStudyAccessService currentStudyAccessService;
    @Mock private AuditService auditService;

    private EcoaService service;

    @BeforeEach
    void setUp() {
        service = new EcoaService(scheduleRepository, assignmentRepository, participantAccessService,
                taskService, currentStudyAccessService, auditService);
    }

    @Test
    void createSchedule_createsParticipantTokenAssignmentAndTask() {
        CreateEcoaScheduleRequest request = createRequest();
        ParticipantAccountDTO account = account();
        IssuedParticipantTokenDTO token = issuedToken();

        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(participantAccessService.findOrCreateAccountForStudySubject(100, 42)).thenReturn(account);
        when(participantAccessService.issueToken(any(), eq(42))).thenReturn(token);
        when(scheduleRepository.save(any(EcoaSchedule.class))).thenAnswer(invocation -> {
            EcoaSchedule schedule = invocation.getArgument(0);
            schedule.setId(7L);
            return schedule;
        });
        when(assignmentRepository.save(any(EcoaAssignment.class))).thenAnswer(invocation -> {
            EcoaAssignment assignment = invocation.getArgument(0);
            if (assignment.getId() == null) {
                assignment.setId(8L);
            }
            return assignment;
        });
        when(taskService.createTaskId(any(CreateTaskRequest.class), eq(42))).thenReturn(9L);

        var result = service.createSchedule(request, 42);

        assertEquals(7L, result.getSchedule().getId());
        assertEquals(8L, result.getAssignment().getId());
        assertEquals(9L, result.getAssignment().getTaskInstanceId());
        assertEquals("/participant/access/raw-token", result.getParticipantEntryUrl());

        verify(participantAccessService).issueToken(argThat(tokenRequest -> {
            assertEquals(5L, tokenRequest.getParticipantAccountId());
            assertEquals("ecoa-questionnaire", tokenRequest.getScope());
            assertNotNull(tokenRequest.getExpiresInHours());
            return true;
        }), eq(42));
        verify(taskService).createTaskId(argThat(taskRequest -> {
            assertEquals(10, taskRequest.getStudyId());
            assertEquals("ISI daily check-in", taskRequest.getTitle());
            assertEquals(TaskTargetType.ECOA_ASSIGNMENT, taskRequest.getTargetType());
            assertEquals(8L, taskRequest.getTargetId());
            return true;
        }), eq(42));
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.CREATE), eq("ecoa_schedule"),
                eq(7L), eq("ISI daily check-in"), isNull(), isNull(), eq(42),
                eq("eCOA schedule created"), eq("ecoa"));
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.ASSIGN), eq("ecoa_assignment"),
                eq(8L), eq("ISI daily check-in"), isNull(), isNull(), eq(42),
                eq("eCOA assignment issued"), eq("ecoa"));
    }

    @Test
    void createSchedule_whenAccessDenied_doesNotCreate() {
        CreateEcoaScheduleRequest request = createRequest();
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.createSchedule(request, 42));
        verifyNoInteractions(participantAccessService, taskService);
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void summarizeAdherence_countsCompletionAndOverdue() {
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(true);
        when(assignmentRepository.findByStudyIdOrderByDueAtAscCreatedDateAsc(10)).thenReturn(List.of(
                assignment(1L, EcoaAssignmentStatus.PENDING),
                assignment(2L, EcoaAssignmentStatus.IN_PROGRESS),
                assignment(3L, EcoaAssignmentStatus.SUBMITTED),
                assignment(4L, EcoaAssignmentStatus.REVIEWED),
                assignment(5L, EcoaAssignmentStatus.OVERDUE)));

        var result = service.summarizeAdherence(10, 42);

        assertEquals(5, result.getTotal());
        assertEquals(1, result.getPending());
        assertEquals(1, result.getInProgress());
        assertEquals(2, result.getCompleted());
        assertEquals(1, result.getOverdue());
        assertEquals(40.0, result.getCompletionRate());
    }

    @Test
    void recordCompletion_marksSubmittedAndCompletesTask() {
        EcoaAssignment assignment = assignment(8L, EcoaAssignmentStatus.PENDING);
        assignment.setTaskInstanceId(9L);
        RecordEcoaCompletionRequest request = new RecordEcoaCompletionRequest();
        request.setQuestionnaireAssignmentId("q-assignment");
        request.setScoreSummary("ISI=12");
        when(assignmentRepository.findById(8L)).thenReturn(Optional.of(assignment));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        var result = service.recordCompletion(8L, request, 42);

        assertEquals(EcoaAssignmentStatus.SUBMITTED, result.getStatus());
        assertEquals("q-assignment", result.getQuestionnaireAssignmentId());
        assertEquals("ISI=12", result.getScoreSummary());
        assertNotNull(result.getCompletedAt());
        verify(taskService).completeTask(9L, 42);
    }

    @Test
    void completeParticipantAssignment_whenTokenMatches_marksSubmittedWithoutOperatorTaskAccess() {
        EcoaAssignment assignment = assignment(8L, EcoaAssignmentStatus.PENDING);
        RecordEcoaCompletionRequest request = new RecordEcoaCompletionRequest();
        request.setScoreSummary("self-reported complete");
        when(participantAccessService.verifyToken("raw-token")).thenReturn(bootstrap());
        when(assignmentRepository.findById(8L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        var result = service.completeParticipantAssignment(8L, "raw-token", request);

        assertEquals(EcoaAssignmentStatus.SUBMITTED, result.getStatus());
        assertEquals("self-reported complete", result.getScoreSummary());
        assertNotNull(result.getCompletedAt());
        verifyNoInteractions(taskService);
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.UPDATE), eq("ecoa_assignment"),
                eq(8L), eq("assignment:8"), isNull(), isNull(), isNull(),
                eq("eCOA assignment completed by participant"), eq("ecoa"));
    }

    @Test
    void expireOverdueAssignments_marksActiveAssignmentsOverdue() {
        EcoaAssignment assignment = assignment(8L, EcoaAssignmentStatus.IN_PROGRESS);
        LocalDateTime now = LocalDateTime.now();
        when(assignmentRepository.findByStatusInAndDueAtBefore(
                Set.of(EcoaAssignmentStatus.PENDING, EcoaAssignmentStatus.IN_PROGRESS), now))
                .thenReturn(List.of(assignment));

        int result = service.expireOverdueAssignments(now);

        assertEquals(1, result);
        assertEquals(EcoaAssignmentStatus.OVERDUE, assignment.getStatus());
        verify(assignmentRepository).saveAll(List.of(assignment));
    }

    private static CreateEcoaScheduleRequest createRequest() {
        CreateEcoaScheduleRequest request = new CreateEcoaScheduleRequest();
        request.setStudyId(10);
        request.setStudySubjectId(100);
        request.setQuestionnaireVersionId("version-1");
        request.setTitle("ISI daily check-in");
        request.setDescription("Complete daily ISI form");
        request.setDueAt(LocalDateTime.now().plusDays(3));
        return request;
    }

    private static ParticipantAccountDTO account() {
        ParticipantAccountDTO account = new ParticipantAccountDTO();
        account.setId(5L);
        account.setStudyId(10);
        account.setStudySubjectId(100);
        account.setDisplayLabel("SS-001");
        return account;
    }

    private static IssuedParticipantTokenDTO issuedToken() {
        ParticipantAccessTokenDTO token = new ParticipantAccessTokenDTO();
        token.setId(6L);
        IssuedParticipantTokenDTO issued = new IssuedParticipantTokenDTO();
        issued.setToken(token);
        issued.setRawToken("raw-token");
        issued.setEntryUrl("/participant/access/raw-token");
        return issued;
    }

    private static ParticipantBootstrapDTO bootstrap() {
        ParticipantBootstrapDTO bootstrap = new ParticipantBootstrapDTO();
        bootstrap.setTokenId(6L);
        bootstrap.setParticipantAccountId(5L);
        bootstrap.setStudyId(10);
        bootstrap.setStudySubjectId(100);
        bootstrap.setScope("ecoa-questionnaire");
        return bootstrap;
    }

    private static EcoaAssignment assignment(Long id, EcoaAssignmentStatus status) {
        EcoaAssignment assignment = new EcoaAssignment();
        assignment.setId(id);
        assignment.setScheduleId(7L);
        assignment.setStudyId(10);
        assignment.setStudySubjectId(100);
        assignment.setParticipantAccountId(5L);
        assignment.setStatus(status);
        assignment.setDueAt(LocalDateTime.now().plusDays(1));
        return assignment;
    }
}
