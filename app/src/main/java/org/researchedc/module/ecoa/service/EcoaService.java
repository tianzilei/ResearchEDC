package org.researchedc.module.ecoa.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.ecoa.dto.CreateEcoaScheduleRequest;
import org.researchedc.module.ecoa.dto.EcoaAdherenceSummaryDTO;
import org.researchedc.module.ecoa.dto.EcoaAssignmentDTO;
import org.researchedc.module.ecoa.dto.EcoaScheduleDTO;
import org.researchedc.module.ecoa.dto.EcoaScheduleResultDTO;
import org.researchedc.module.ecoa.dto.RecordEcoaCompletionRequest;
import org.researchedc.module.ecoa.entity.EcoaAssignment;
import org.researchedc.module.ecoa.entity.EcoaSchedule;
import org.researchedc.module.ecoa.enums.EcoaAssignmentStatus;
import org.researchedc.module.ecoa.repository.EcoaAssignmentRepository;
import org.researchedc.module.ecoa.repository.EcoaScheduleRepository;
import org.researchedc.module.participantaccess.dto.IssueParticipantTokenRequest;
import org.researchedc.module.participantaccess.dto.IssuedParticipantTokenDTO;
import org.researchedc.module.participantaccess.dto.ParticipantAccountDTO;
import org.researchedc.module.participantaccess.dto.ParticipantBootstrapDTO;
import org.researchedc.module.participantaccess.service.ParticipantAccessService;
import org.researchedc.module.task.dto.CreateTaskRequest;
import org.researchedc.module.task.enums.TaskTargetType;
import org.researchedc.module.task.service.TaskService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class EcoaService {

    private static final Set<EcoaAssignmentStatus> ACTIVE_STATUSES = EnumSet.of(
            EcoaAssignmentStatus.PENDING, EcoaAssignmentStatus.IN_PROGRESS);

    private final EcoaScheduleRepository scheduleRepository;
    private final EcoaAssignmentRepository assignmentRepository;
    private final ParticipantAccessService participantAccessService;
    private final TaskService taskService;
    private final CurrentStudyAccessService currentStudyAccessService;
    private final AuditService auditService;

    public EcoaService(EcoaScheduleRepository scheduleRepository,
                       EcoaAssignmentRepository assignmentRepository,
                       ParticipantAccessService participantAccessService,
                       TaskService taskService,
                       CurrentStudyAccessService currentStudyAccessService,
                       AuditService auditService) {
        this.scheduleRepository = scheduleRepository;
        this.assignmentRepository = assignmentRepository;
        this.participantAccessService = participantAccessService;
        this.taskService = taskService;
        this.currentStudyAccessService = currentStudyAccessService;
        this.auditService = auditService;
    }

    public List<EcoaSchedule> listSchedules(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return scheduleRepository.findByStudyIdOrderByDueAtAscCreatedDateAsc(studyId);
    }

    public List<EcoaAssignment> listAssignments(Integer studyId, Integer currentUserId) {
        if (studyId != null) {
            requireReadAccess(currentUserId, studyId);
            return assignmentRepository.findByStudyIdOrderByDueAtAscCreatedDateAsc(studyId);
        }
        if (currentStudyAccessService.canReadAllStudies(currentUserId)) {
            return assignmentRepository.findAll();
        }
        Set<Integer> studyIds = currentStudyAccessService.readableStudyIds(currentUserId);
        if (studyIds.isEmpty()) {
            return List.of();
        }
        return assignmentRepository.findByStudyIdInOrderByDueAtAscCreatedDateAsc(studyIds);
    }

    public EcoaAdherenceSummaryDTO summarizeAdherence(Integer studyId, Integer currentUserId) {
        List<EcoaAssignment> assignments = listAssignments(studyId, currentUserId);
        EcoaAdherenceSummaryDTO summary = new EcoaAdherenceSummaryDTO();
        summary.setTotal(assignments.size());
        summary.setPending(count(assignments, EcoaAssignmentStatus.PENDING));
        summary.setInProgress(count(assignments, EcoaAssignmentStatus.IN_PROGRESS));
        long completed = count(assignments, EcoaAssignmentStatus.SUBMITTED)
                + count(assignments, EcoaAssignmentStatus.REVIEWED);
        summary.setCompleted(completed);
        summary.setOverdue(count(assignments, EcoaAssignmentStatus.OVERDUE));
        summary.setCompletionRate(assignments.isEmpty()
                ? 0.0
                : Math.round((completed * 10000.0) / assignments.size()) / 100.0);
        return summary;
    }

    @Transactional
    public List<EcoaAssignment> listParticipantAssignments(String rawToken) {
        ParticipantBootstrapDTO bootstrap = participantAccessService.verifyToken(rawToken);
        return listParticipantAssignmentsForAccount(bootstrap.getParticipantAccountId());
    }

    public List<EcoaAssignment> listParticipantAssignmentsForAccount(Long participantAccountId) {
        return assignmentRepository.findByParticipantAccountIdAndStatusInOrderByDueAtAscCreatedDateAsc(
                participantAccountId,
                ACTIVE_STATUSES);
    }

    @Transactional
    public EcoaAssignment completeParticipantAssignment(Long assignmentId, String rawToken,
                                                       RecordEcoaCompletionRequest request) {
        ParticipantBootstrapDTO bootstrap = participantAccessService.verifyToken(rawToken);
        EcoaAssignment assignment = findAssignment(assignmentId);
        if (!assignment.getParticipantAccountId().equals(bootstrap.getParticipantAccountId())) {
            throw new AccessDeniedException("eCOA assignment does not belong to this participant");
        }
        if (assignment.getStatus() == EcoaAssignmentStatus.CANCELLED
                || assignment.getStatus() == EcoaAssignmentStatus.OVERDUE) {
            throw new IllegalStateException("eCOA assignment is not active");
        }
        assignment.setStatus(EcoaAssignmentStatus.SUBMITTED);
        assignment.setCompletedAt(request.getCompletedAt() == null ? LocalDateTime.now() : request.getCompletedAt());
        assignment.setQuestionnaireAssignmentId(blankToNull(request.getQuestionnaireAssignmentId()));
        assignment.setScoreSummary(defaultText(request.getScoreSummary()));
        assignment.setUpdatedDate(LocalDateTime.now());
        EcoaAssignment saved = assignmentRepository.save(assignment);
        record(saved.getStudyId(), AuditEventType.UPDATE, "ecoa_assignment", saved.getId(),
                "assignment:" + saved.getId(), null, "eCOA assignment completed by participant");
        return saved;
    }

    @Transactional
    public EcoaScheduleResultDTO createSchedule(CreateEcoaScheduleRequest request, Integer currentUserId) {
        validateScheduleRequest(request);
        requireWriteAccess(currentUserId, request.getStudyId());

        ParticipantAccountDTO participantAccount =
                participantAccessService.findOrCreateAccountForStudySubject(request.getStudySubjectId(), currentUserId);

        EcoaSchedule schedule = new EcoaSchedule();
        schedule.setStudyId(request.getStudyId());
        schedule.setStudySubjectId(request.getStudySubjectId());
        schedule.setStudyEventId(request.getStudyEventId());
        schedule.setQuestionnaireVersionId(request.getQuestionnaireVersionId().trim());
        schedule.setTitle(request.getTitle().trim());
        schedule.setDescription(defaultText(request.getDescription()));
        schedule.setDueAt(request.getDueAt());
        schedule.setWindowOpensAt(request.getWindowOpensAt());
        schedule.setWindowClosesAt(request.getWindowClosesAt());
        schedule.setCreatedBy(currentUserId);
        EcoaSchedule savedSchedule = scheduleRepository.save(schedule);

        IssuedParticipantTokenDTO issuedToken =
                participantAccessService.issueToken(tokenRequest(participantAccount.getId(), request.getDueAt()), currentUserId);

        EcoaAssignment assignment = new EcoaAssignment();
        assignment.setScheduleId(savedSchedule.getId());
        assignment.setStudyId(savedSchedule.getStudyId());
        assignment.setStudySubjectId(savedSchedule.getStudySubjectId());
        assignment.setParticipantAccountId(participantAccount.getId());
        assignment.setParticipantTokenId(issuedToken.getToken().getId());
        assignment.setQuestionnaireAssignmentId(null);
        assignment.setStatus(resolveInitialStatus(savedSchedule.getDueAt(), LocalDateTime.now()));
        assignment.setDueAt(savedSchedule.getDueAt());
        assignment.setWindowOpensAt(savedSchedule.getWindowOpensAt());
        assignment.setWindowClosesAt(savedSchedule.getWindowClosesAt());
        assignment.setEntryUrl(issuedToken.getEntryUrl());
        assignment.setCreatedBy(currentUserId);
        EcoaAssignment savedAssignment = assignmentRepository.save(assignment);

        Long taskInstanceId = taskService.createTaskId(taskRequest(savedSchedule, savedAssignment), currentUserId);
        savedAssignment.setTaskInstanceId(taskInstanceId);
        savedAssignment.setUpdatedDate(LocalDateTime.now());
        savedAssignment = assignmentRepository.save(savedAssignment);

        record(savedSchedule.getStudyId(), AuditEventType.CREATE, "ecoa_schedule", savedSchedule.getId(),
                savedSchedule.getTitle(), currentUserId, "eCOA schedule created");
        record(savedAssignment.getStudyId(), AuditEventType.ASSIGN, "ecoa_assignment", savedAssignment.getId(),
                savedSchedule.getTitle(), currentUserId, "eCOA assignment issued");

        EcoaScheduleResultDTO result = new EcoaScheduleResultDTO();
        result.setSchedule(toScheduleDto(savedSchedule));
        result.setAssignment(toAssignmentDto(savedAssignment));
        result.setParticipantEntryUrl(issuedToken.getEntryUrl());
        return result;
    }

    @Transactional
    public EcoaAssignment recordCompletion(Long assignmentId, RecordEcoaCompletionRequest request,
                                           Integer currentUserId) {
        EcoaAssignment assignment = findAssignment(assignmentId);
        requireWriteAccess(currentUserId, assignment.getStudyId());
        if (assignment.getStatus() == EcoaAssignmentStatus.CANCELLED) {
            throw new IllegalStateException("eCOA assignment is cancelled");
        }
        assignment.setStatus(EcoaAssignmentStatus.SUBMITTED);
        assignment.setCompletedAt(request.getCompletedAt() == null ? LocalDateTime.now() : request.getCompletedAt());
        assignment.setQuestionnaireAssignmentId(blankToNull(request.getQuestionnaireAssignmentId()));
        assignment.setScoreSummary(defaultText(request.getScoreSummary()));
        assignment.setUpdatedDate(LocalDateTime.now());
        EcoaAssignment saved = assignmentRepository.save(assignment);
        if (saved.getTaskInstanceId() != null) {
            taskService.completeTask(saved.getTaskInstanceId(), currentUserId);
        }
        record(saved.getStudyId(), AuditEventType.UPDATE, "ecoa_assignment", saved.getId(),
                "assignment:" + saved.getId(), currentUserId, "eCOA assignment completed");
        return saved;
    }

    @Transactional
    public EcoaAssignment cancelAssignment(Long assignmentId, Integer currentUserId) {
        EcoaAssignment assignment = findAssignment(assignmentId);
        requireWriteAccess(currentUserId, assignment.getStudyId());
        if (assignment.getStatus() == EcoaAssignmentStatus.SUBMITTED
                || assignment.getStatus() == EcoaAssignmentStatus.REVIEWED) {
            throw new IllegalStateException("Completed eCOA assignment cannot be cancelled");
        }
        assignment.setStatus(EcoaAssignmentStatus.CANCELLED);
        assignment.setUpdatedDate(LocalDateTime.now());
        EcoaAssignment saved = assignmentRepository.save(assignment);
        if (saved.getTaskInstanceId() != null) {
            taskService.cancelTask(saved.getTaskInstanceId(), currentUserId);
        }
        record(saved.getStudyId(), AuditEventType.UPDATE, "ecoa_assignment", saved.getId(),
                "assignment:" + saved.getId(), currentUserId, "eCOA assignment cancelled");
        return saved;
    }

    @Transactional
    public int expireOverdueAssignments(LocalDateTime now) {
        List<EcoaAssignment> overdue = assignmentRepository.findByStatusInAndDueAtBefore(ACTIVE_STATUSES, now);
        overdue.forEach(assignment -> {
            assignment.setStatus(EcoaAssignmentStatus.OVERDUE);
            assignment.setUpdatedDate(now);
        });
        assignmentRepository.saveAll(overdue);
        return overdue.size();
    }

    public EcoaScheduleDTO toScheduleDto(EcoaSchedule schedule) {
        EcoaScheduleDTO dto = new EcoaScheduleDTO();
        dto.setId(schedule.getId());
        dto.setStudyId(schedule.getStudyId());
        dto.setStudySubjectId(schedule.getStudySubjectId());
        dto.setStudyEventId(schedule.getStudyEventId());
        dto.setQuestionnaireVersionId(schedule.getQuestionnaireVersionId());
        dto.setTitle(schedule.getTitle());
        dto.setDescription(schedule.getDescription());
        dto.setDueAt(schedule.getDueAt());
        dto.setWindowOpensAt(schedule.getWindowOpensAt());
        dto.setWindowClosesAt(schedule.getWindowClosesAt());
        dto.setCreatedBy(schedule.getCreatedBy());
        dto.setCreatedDate(schedule.getCreatedDate());
        return dto;
    }

    public EcoaAssignmentDTO toAssignmentDto(EcoaAssignment assignment) {
        EcoaAssignmentDTO dto = new EcoaAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setScheduleId(assignment.getScheduleId());
        dto.setStudyId(assignment.getStudyId());
        dto.setStudySubjectId(assignment.getStudySubjectId());
        dto.setParticipantAccountId(assignment.getParticipantAccountId());
        dto.setParticipantTokenId(assignment.getParticipantTokenId());
        dto.setTaskInstanceId(assignment.getTaskInstanceId());
        dto.setQuestionnaireAssignmentId(assignment.getQuestionnaireAssignmentId());
        dto.setStatus(assignment.getStatus());
        dto.setDueAt(assignment.getDueAt());
        dto.setWindowOpensAt(assignment.getWindowOpensAt());
        dto.setWindowClosesAt(assignment.getWindowClosesAt());
        dto.setEntryUrl(assignment.getEntryUrl());
        dto.setCompletedAt(assignment.getCompletedAt());
        dto.setScoreSummary(assignment.getScoreSummary());
        dto.setCreatedBy(assignment.getCreatedBy());
        dto.setCreatedDate(assignment.getCreatedDate());
        dto.setUpdatedDate(assignment.getUpdatedDate());
        return dto;
    }

    private EcoaAssignment findAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("eCOA assignment not found: " + id));
    }

    private void validateScheduleRequest(CreateEcoaScheduleRequest request) {
        if (request.getStudyId() == null) {
            throw new IllegalArgumentException("studyId is required");
        }
        if (request.getStudySubjectId() == null) {
            throw new IllegalArgumentException("studySubjectId is required");
        }
        if (!StringUtils.hasText(request.getQuestionnaireVersionId())) {
            throw new IllegalArgumentException("questionnaireVersionId is required");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw new IllegalArgumentException("title is required");
        }
        if (request.getDueAt() == null) {
            throw new IllegalArgumentException("dueAt is required");
        }
        if (request.getWindowOpensAt() != null && request.getWindowOpensAt().isAfter(request.getDueAt())) {
            throw new IllegalArgumentException("windowOpensAt must be before dueAt");
        }
        if (request.getWindowClosesAt() != null && request.getWindowClosesAt().isBefore(request.getDueAt())) {
            throw new IllegalArgumentException("windowClosesAt must be after dueAt");
        }
    }

    private IssueParticipantTokenRequest tokenRequest(Long participantAccountId, LocalDateTime dueAt) {
        IssueParticipantTokenRequest request = new IssueParticipantTokenRequest();
        request.setParticipantAccountId(participantAccountId);
        request.setScope("ecoa-questionnaire");
        request.setExpiresInHours(resolveTokenExpiryHours(dueAt));
        return request;
    }

    private Integer resolveTokenExpiryHours(LocalDateTime dueAt) {
        long hours = Duration.between(LocalDateTime.now(), dueAt.plusDays(1)).toHours();
        if (hours < 1) {
            return 1;
        }
        return (int) Math.min(hours, 24L * 90L);
    }

    private CreateTaskRequest taskRequest(EcoaSchedule schedule, EcoaAssignment assignment) {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setStudyId(schedule.getStudyId());
        request.setTitle(schedule.getTitle());
        request.setDescription(schedule.getDescription());
        request.setTargetType(TaskTargetType.ECOA_ASSIGNMENT);
        request.setTargetId(assignment.getId());
        request.setDueDate(schedule.getDueAt());
        return request;
    }

    private EcoaAssignmentStatus resolveInitialStatus(LocalDateTime dueAt, LocalDateTime now) {
        return dueAt.isBefore(now) ? EcoaAssignmentStatus.OVERDUE : EcoaAssignmentStatus.PENDING;
    }

    private long count(List<EcoaAssignment> assignments, EcoaAssignmentStatus status) {
        return assignments.stream().filter(assignment -> assignment.getStatus() == status).count();
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

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void record(Integer studyId, AuditEventType eventType, String entityType, Long entityId,
                        String entityLabel, Integer performedBy, String details) {
        auditService.recordAudit(studyId, eventType, entityType, entityId, entityLabel,
                null, null, performedBy, details, "ecoa");
    }
}
