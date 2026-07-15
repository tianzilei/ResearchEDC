package org.researchedc.module.task.dto;

import java.time.LocalDateTime;
import org.researchedc.module.task.enums.TaskStatus;
import org.researchedc.module.task.enums.TaskTargetType;

public class TaskInstanceDTO {
    private Long id;
    private Long templateId;
    private Integer studyId;
    private Integer assignedTo;
    private String title;
    private String description;
    private TaskTargetType targetType;
    private Long targetId;
    private TaskStatus status;
    private LocalDateTime dueDate;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer completedBy;
    private LocalDateTime completedDate;
    private Integer cancelledBy;
    private LocalDateTime cancelledDate;
    private LocalDateTime lastReminderDate;
    private Integer reminderCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Integer assignedTo) { this.assignedTo = assignedTo; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskTargetType getTargetType() { return targetType; }
    public void setTargetType(TaskTargetType targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public Integer getCompletedBy() { return completedBy; }
    public void setCompletedBy(Integer completedBy) { this.completedBy = completedBy; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public Integer getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(Integer cancelledBy) { this.cancelledBy = cancelledBy; }

    public LocalDateTime getCancelledDate() { return cancelledDate; }
    public void setCancelledDate(LocalDateTime cancelledDate) { this.cancelledDate = cancelledDate; }

    public LocalDateTime getLastReminderDate() { return lastReminderDate; }
    public void setLastReminderDate(LocalDateTime lastReminderDate) { this.lastReminderDate = lastReminderDate; }

    public Integer getReminderCount() { return reminderCount; }
    public void setReminderCount(Integer reminderCount) { this.reminderCount = reminderCount; }
}
