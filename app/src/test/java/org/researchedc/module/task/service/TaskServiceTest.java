package org.researchedc.module.task.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
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
import org.researchedc.module.task.dto.CreateTaskRequest;
import org.researchedc.module.task.dto.CreateTaskTemplateRequest;
import org.researchedc.module.task.entity.TaskInstance;
import org.researchedc.module.task.entity.TaskTemplate;
import org.researchedc.module.task.enums.TaskStatus;
import org.researchedc.module.task.enums.TaskTargetType;
import org.researchedc.module.task.repository.TaskInstanceRepository;
import org.researchedc.module.task.repository.TaskTemplateRepository;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskTemplateRepository templateRepository;
    @Mock private TaskInstanceRepository taskRepository;
    @Mock private CurrentStudyAccessService currentStudyAccessService;
    @Mock private AuditService auditService;
    @Mock private TaskReminderDispatcher reminderDispatcher;

    private TaskService service;

    @BeforeEach
    void setUp() {
        service = new TaskService(templateRepository, taskRepository, currentStudyAccessService,
                auditService, reminderDispatcher);
    }

    @Test
    void createTemplate_whenValid_savesAndAudits() {
        CreateTaskTemplateRequest request = new CreateTaskTemplateRequest();
        request.setStudyId(10);
        request.setName("Review CRF");
        request.setTaskType("CRF_REVIEW");
        request.setDefaultDueDays(3);
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(templateRepository.save(any(TaskTemplate.class))).thenAnswer(invocation -> {
            TaskTemplate template = invocation.getArgument(0);
            template.setId(7L);
            return template;
        });

        TaskTemplate result = service.createTemplate(request, 42);

        assertEquals(7L, result.getId());
        assertEquals("Review CRF", result.getName());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.CREATE), eq("task_template"),
                eq(7L), eq("Review CRF"), isNull(), isNull(), eq(42), eq("Task template created"),
                eq("task"));
    }

    @Test
    void createTask_whenTemplateProvidesDueDate_usesTemplateDefault() {
        TaskTemplate template = template(3L, 10, "CRF_REVIEW", 2);
        CreateTaskRequest request = createTaskRequest(10, "Review visit", null);
        request.setTemplateId(3L);
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(templateRepository.findById(3L)).thenReturn(Optional.of(template));
        when(taskRepository.save(any(TaskInstance.class))).thenAnswer(invocation -> {
            TaskInstance task = invocation.getArgument(0);
            task.setId(11L);
            return task;
        });

        TaskInstance result = service.createTask(request, 42);

        assertEquals(11L, result.getId());
        assertEquals(TaskStatus.PENDING, result.getStatus());
        assertEquals(TaskTargetType.STUDY, result.getTargetType());
        assertNotNull(result.getDueDate());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.CREATE), eq("task_instance"),
                eq(11L), eq("Review visit"), isNull(), isNull(), eq(42), eq("Task created"),
                eq("task"));
    }

    @Test
    void createTask_whenDueDatePast_setsOverdue() {
        CreateTaskRequest request = createTaskRequest(10, "Late task", LocalDateTime.now().minusDays(1));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(taskRepository.save(any(TaskInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskInstance result = service.createTask(request, 42);

        assertEquals(TaskStatus.OVERDUE, result.getStatus());
    }

    @Test
    void createTask_whenTemplateStudyDiffers_throws() {
        CreateTaskRequest request = createTaskRequest(10, "Wrong study", null);
        request.setTemplateId(3L);
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(templateRepository.findById(3L)).thenReturn(Optional.of(template(3L, 20, "TYPE", 1)));

        assertThrows(IllegalArgumentException.class, () -> service.createTask(request, 42));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void listTasks_withoutStudy_filtersToReadableStudies() {
        TaskInstance task = task(1L, 10, TaskStatus.PENDING);
        when(currentStudyAccessService.canReadAllStudies(42)).thenReturn(false);
        when(currentStudyAccessService.readableStudyIds(42)).thenReturn(Set.of(10, 11));
        when(taskRepository.findByStudyIdInOrderByDueDateAscCreatedDateAsc(Set.of(10, 11)))
                .thenReturn(List.of(task));

        List<TaskInstance> result = service.listTasks(null, TaskStatus.PENDING, 42);

        assertEquals(List.of(task), result);
    }

    @Test
    void getTask_whenAccessDenied_throws() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task(1L, 10, TaskStatus.PENDING)));
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getTask(1L, 42));
    }

    @Test
    void completeTask_whenActive_marksCompletedAndAudits() {
        TaskInstance task = task(1L, 10, TaskStatus.PENDING);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(taskRepository.save(task)).thenReturn(task);

        TaskInstance result = service.completeTask(1L, 42);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertEquals(42, result.getCompletedBy());
        assertNotNull(result.getCompletedDate());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.UPDATE), eq("task_instance"),
                eq(1L), eq("Task 1"), isNull(), isNull(), eq(42), eq("Task completed"),
                eq("task"));
    }

    @Test
    void cancelTask_whenAlreadyCompleted_throws() {
        TaskInstance task = task(1L, 10, TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.cancelTask(1L, 42));
    }

    @Test
    void expireOverdueTasks_marksPendingDueTasksOverdue() {
        TaskInstance task = task(1L, 10, TaskStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();
        when(taskRepository.findByStatusInAndDueDateBefore(Set.of(TaskStatus.PENDING, TaskStatus.DUE), now))
                .thenReturn(List.of(task));

        int count = service.expireOverdueTasks(now);

        assertEquals(1, count);
        assertEquals(TaskStatus.OVERDUE, task.getStatus());
        verify(taskRepository).saveAll(List.of(task));
    }

    @Test
    void dispatchReminder_usesDispatcherAndIncrementsCount() {
        TaskInstance task = task(1L, 10, TaskStatus.DUE);
        task.setReminderCount(2);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(taskRepository.save(task)).thenReturn(task);

        TaskInstance result = service.dispatchReminder(1L, 42);

        assertEquals(3, result.getReminderCount());
        assertNotNull(result.getLastReminderDate());
        verify(reminderDispatcher).dispatchReminder(task);
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.SYSTEM), eq("task_instance"),
                eq(1L), eq("Task 1"), isNull(), isNull(), eq(42),
                eq("Task reminder dispatched"), eq("task"));
    }

    @Test
    void findTask_whenMissing_throws() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getTask(99L, 42));
    }

    private static CreateTaskRequest createTaskRequest(Integer studyId, String title, LocalDateTime dueDate) {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setStudyId(studyId);
        request.setTitle(title);
        request.setDueDate(dueDate);
        return request;
    }

    private static TaskTemplate template(Long id, Integer studyId, String taskType, Integer defaultDueDays) {
        TaskTemplate template = new TaskTemplate();
        template.setId(id);
        template.setStudyId(studyId);
        template.setName("Template");
        template.setTaskType(taskType);
        template.setDefaultDueDays(defaultDueDays);
        return template;
    }

    private static TaskInstance task(Long id, Integer studyId, TaskStatus status) {
        TaskInstance task = new TaskInstance();
        task.setId(id);
        task.setStudyId(studyId);
        task.setTitle("Task " + id);
        task.setStatus(status);
        task.setTargetType(TaskTargetType.STUDY);
        task.setReminderCount(0);
        return task;
    }
}
