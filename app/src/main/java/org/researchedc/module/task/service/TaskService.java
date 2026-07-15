package org.researchedc.module.task.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.task.dto.CreateTaskRequest;
import org.researchedc.module.task.dto.CreateTaskTemplateRequest;
import org.researchedc.module.task.entity.TaskInstance;
import org.researchedc.module.task.entity.TaskTemplate;
import org.researchedc.module.task.enums.TaskStatus;
import org.researchedc.module.task.enums.TaskTargetType;
import org.researchedc.module.task.repository.TaskInstanceRepository;
import org.researchedc.module.task.repository.TaskTemplateRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final Set<TaskStatus> ACTIVE_STATUSES = Set.of(
            TaskStatus.PENDING, TaskStatus.DUE, TaskStatus.OVERDUE);

    private final TaskTemplateRepository templateRepository;
    private final TaskInstanceRepository taskRepository;
    private final CurrentStudyAccessService currentStudyAccessService;
    private final AuditService auditService;
    private final TaskReminderDispatcher reminderDispatcher;

    public TaskService(TaskTemplateRepository templateRepository,
                       TaskInstanceRepository taskRepository,
                       CurrentStudyAccessService currentStudyAccessService,
                       AuditService auditService,
                       TaskReminderDispatcher reminderDispatcher) {
        this.templateRepository = templateRepository;
        this.taskRepository = taskRepository;
        this.currentStudyAccessService = currentStudyAccessService;
        this.auditService = auditService;
        this.reminderDispatcher = reminderDispatcher;
    }

    public List<TaskTemplate> listTemplates(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return templateRepository.findByStudyIdOrderByNameAsc(studyId);
    }

    @Transactional
    public TaskTemplate createTemplate(CreateTaskTemplateRequest request, Integer currentUserId) {
        requireWriteAccess(currentUserId, request.getStudyId());
        requireText(request.getName(), "Task template name is required");
        requireText(request.getTaskType(), "Task type is required");

        TaskTemplate template = new TaskTemplate();
        template.setStudyId(request.getStudyId());
        template.setName(request.getName().trim());
        template.setDescription(defaultText(request.getDescription()));
        template.setTaskType(request.getTaskType().trim());
        template.setDefaultDueDays(request.getDefaultDueDays());
        template.setActive(true);
        template.setCreatedBy(currentUserId);
        TaskTemplate saved = templateRepository.save(template);
        record(saved.getStudyId(), AuditEventType.CREATE, "task_template", saved.getId(),
                saved.getName(), currentUserId, "Task template created");
        return saved;
    }

    public List<TaskInstance> listTasks(Integer studyId, TaskStatus status, Integer currentUserId) {
        if (studyId != null) {
            requireReadAccess(currentUserId, studyId);
            return status == null
                    ? taskRepository.findByStudyIdOrderByDueDateAscCreatedDateAsc(studyId)
                    : taskRepository.findByStudyIdAndStatusOrderByDueDateAscCreatedDateAsc(studyId, status);
        }
        if (currentStudyAccessService.canReadAllStudies(currentUserId)) {
            return taskRepository.findAll();
        }
        Set<Integer> studyIds = currentStudyAccessService.readableStudyIds(currentUserId);
        if (studyIds.isEmpty()) {
            return List.of();
        }
        return taskRepository.findByStudyIdInOrderByDueDateAscCreatedDateAsc(studyIds).stream()
                .filter(task -> status == null || task.getStatus() == status)
                .toList();
    }

    public List<TaskInstance> listMyActiveTasks(Integer currentUserId) {
        return taskRepository.findByAssignedToAndStatusInOrderByDueDateAscCreatedDateAsc(
                currentUserId, ACTIVE_STATUSES).stream()
                .filter(task -> currentStudyAccessService.canReadStudy(currentUserId, task.getStudyId()))
                .toList();
    }

    public TaskInstance getTask(Long id, Integer currentUserId) {
        TaskInstance task = findTask(id);
        requireReadAccess(currentUserId, task.getStudyId());
        return task;
    }

    @Transactional
    public TaskInstance createTask(CreateTaskRequest request, Integer currentUserId) {
        requireWriteAccess(currentUserId, request.getStudyId());
        requireText(request.getTitle(), "Task title is required");

        TaskTemplate template = null;
        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Task template not found: " + request.getTemplateId()));
            if (!request.getStudyId().equals(template.getStudyId())) {
                throw new IllegalArgumentException("Task template belongs to a different study");
            }
        }

        TaskInstance task = new TaskInstance();
        task.setTemplateId(request.getTemplateId());
        task.setStudyId(request.getStudyId());
        task.setAssignedTo(request.getAssignedTo());
        task.setTitle(request.getTitle().trim());
        task.setDescription(defaultText(request.getDescription()));
        task.setTargetType(request.getTargetType() != null ? request.getTargetType() : TaskTargetType.STUDY);
        task.setTargetId(request.getTargetId());
        task.setDueDate(resolveDueDate(request.getDueDate(), template));
        task.setStatus(resolveInitialStatus(task.getDueDate(), LocalDateTime.now()));
        task.setCreatedBy(currentUserId);
        TaskInstance saved = taskRepository.save(task);
        record(saved.getStudyId(), AuditEventType.CREATE, "task_instance", saved.getId(),
                saved.getTitle(), currentUserId, "Task created");
        return saved;
    }

    @Transactional
    public Long createTaskId(CreateTaskRequest request, Integer currentUserId) {
        return createTask(request, currentUserId).getId();
    }

    @Transactional
    public TaskInstance completeTask(Long id, Integer currentUserId) {
        TaskInstance task = findTask(id);
        requireWriteAccess(currentUserId, task.getStudyId());
        requireActive(task);
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedBy(currentUserId);
        task.setCompletedDate(LocalDateTime.now());
        TaskInstance saved = taskRepository.save(task);
        record(saved.getStudyId(), AuditEventType.UPDATE, "task_instance", saved.getId(),
                saved.getTitle(), currentUserId, "Task completed");
        return saved;
    }

    @Transactional
    public TaskInstance cancelTask(Long id, Integer currentUserId) {
        TaskInstance task = findTask(id);
        requireWriteAccess(currentUserId, task.getStudyId());
        requireActive(task);
        task.setStatus(TaskStatus.CANCELLED);
        task.setCancelledBy(currentUserId);
        task.setCancelledDate(LocalDateTime.now());
        TaskInstance saved = taskRepository.save(task);
        record(saved.getStudyId(), AuditEventType.UPDATE, "task_instance", saved.getId(),
                saved.getTitle(), currentUserId, "Task cancelled");
        return saved;
    }

    @Transactional
    public int expireOverdueTasks(LocalDateTime now) {
        List<TaskInstance> overdue = taskRepository.findByStatusInAndDueDateBefore(
                Set.of(TaskStatus.PENDING, TaskStatus.DUE), now);
        overdue.forEach(task -> task.setStatus(TaskStatus.OVERDUE));
        taskRepository.saveAll(overdue);
        return overdue.size();
    }

    @Transactional
    public TaskInstance dispatchReminder(Long id, Integer currentUserId) {
        TaskInstance task = findTask(id);
        requireWriteAccess(currentUserId, task.getStudyId());
        requireActive(task);
        reminderDispatcher.dispatchReminder(task);
        task.setLastReminderDate(LocalDateTime.now());
        task.setReminderCount(task.getReminderCount() == null ? 1 : task.getReminderCount() + 1);
        TaskInstance saved = taskRepository.save(task);
        record(saved.getStudyId(), AuditEventType.SYSTEM, "task_instance", saved.getId(),
                saved.getTitle(), currentUserId, "Task reminder dispatched");
        return saved;
    }

    private TaskInstance findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + id));
    }

    private LocalDateTime resolveDueDate(LocalDateTime dueDate, TaskTemplate template) {
        if (dueDate != null) {
            return dueDate;
        }
        if (template != null && template.getDefaultDueDays() != null) {
            return LocalDateTime.now().plusDays(template.getDefaultDueDays());
        }
        return null;
    }

    private TaskStatus resolveInitialStatus(LocalDateTime dueDate, LocalDateTime now) {
        if (dueDate == null || dueDate.isAfter(now)) {
            return TaskStatus.PENDING;
        }
        return TaskStatus.OVERDUE;
    }

    private void requireActive(TaskInstance task) {
        if (!ACTIVE_STATUSES.contains(task.getStatus())) {
            throw new IllegalStateException("Task is not active: " + task.getId());
        }
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

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private void record(Integer studyId, AuditEventType eventType, String entityType, Long entityId,
                        String entityLabel, Integer performedBy, String details) {
        auditService.recordAudit(studyId, eventType, entityType, entityId, entityLabel,
                null, null, performedBy, details, "task");
    }
}
