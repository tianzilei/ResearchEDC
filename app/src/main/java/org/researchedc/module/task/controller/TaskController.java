package org.researchedc.module.task.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.task.dto.CreateTaskRequest;
import org.researchedc.module.task.dto.CreateTaskTemplateRequest;
import org.researchedc.module.task.dto.TaskInstanceDTO;
import org.researchedc.module.task.dto.TaskTemplateDTO;
import org.researchedc.module.task.entity.TaskInstance;
import org.researchedc.module.task.entity.TaskTemplate;
import org.researchedc.module.task.enums.TaskStatus;
import org.researchedc.module.task.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;
    private final CurrentUserUtils currentUserUtils;

    public TaskController(TaskService taskService, CurrentUserUtils currentUserUtils) {
        this.taskService = taskService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/templates")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<TaskTemplateDTO>> listTemplates(@RequestParam Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(taskService.listTemplates(studyId, currentUserId).stream()
                .map(this::toTemplateDto)
                .toList());
    }

    @PostMapping("/templates")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<TaskTemplateDTO> createTemplate(@RequestBody CreateTaskTemplateRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toTemplateDto(taskService.createTemplate(request, currentUserId)));
    }

    @GetMapping
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<TaskInstanceDTO>> listTasks(@RequestParam(required = false) Integer studyId,
                                                           @RequestParam(required = false) TaskStatus status,
                                                           @RequestParam(defaultValue = "false") boolean assignedToMe) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        List<TaskInstance> tasks = assignedToMe
                ? taskService.listMyActiveTasks(currentUserId)
                : taskService.listTasks(studyId, status, currentUserId);
        return ResponseEntity.ok(tasks.stream().map(this::toTaskDto).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<TaskInstanceDTO> getTask(@PathVariable Long id) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(toTaskDto(taskService.getTask(id, currentUserId)));
    }

    @PostMapping
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<TaskInstanceDTO> createTask(@RequestBody CreateTaskRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toTaskDto(taskService.createTask(request, currentUserId)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<TaskInstanceDTO> completeTask(@PathVariable Long id) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(toTaskDto(taskService.completeTask(id, currentUserId)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<TaskInstanceDTO> cancelTask(@PathVariable Long id) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(toTaskDto(taskService.cancelTask(id, currentUserId)));
    }

    @PostMapping("/expire-overdue")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Integer> expireOverdueTasks() {
        return ResponseEntity.ok(taskService.expireOverdueTasks(LocalDateTime.now()));
    }

    @PostMapping("/{id}/reminders")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<TaskInstanceDTO> dispatchReminder(@PathVariable Long id) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(toTaskDto(taskService.dispatchReminder(id, currentUserId)));
    }

    private TaskTemplateDTO toTemplateDto(TaskTemplate template) {
        TaskTemplateDTO dto = new TaskTemplateDTO();
        dto.setId(template.getId());
        dto.setStudyId(template.getStudyId());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());
        dto.setTaskType(template.getTaskType());
        dto.setDefaultDueDays(template.getDefaultDueDays());
        dto.setActive(template.getActive());
        dto.setCreatedBy(template.getCreatedBy());
        dto.setCreatedDate(template.getCreatedDate());
        return dto;
    }

    private TaskInstanceDTO toTaskDto(TaskInstance task) {
        TaskInstanceDTO dto = new TaskInstanceDTO();
        dto.setId(task.getId());
        dto.setTemplateId(task.getTemplateId());
        dto.setStudyId(task.getStudyId());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setTargetType(task.getTargetType());
        dto.setTargetId(task.getTargetId());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setCreatedDate(task.getCreatedDate());
        dto.setCompletedBy(task.getCompletedBy());
        dto.setCompletedDate(task.getCompletedDate());
        dto.setCancelledBy(task.getCancelledBy());
        dto.setCancelledDate(task.getCancelledDate());
        dto.setLastReminderDate(task.getLastReminderDate());
        dto.setReminderCount(task.getReminderCount());
        return dto;
    }
}
