package org.researchedc.module.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.researchedc.module.task.enums.TaskStatus;
import org.researchedc.module.task.enums.TaskTargetType;

@Entity(name = "ModuleTaskInstance")
@Table(name = "task_instance")
public class TaskInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "study_id", nullable = false)
    private Integer studyId;

    @Column(name = "assigned_to")
    private Integer assignedTo;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "target_type", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private TaskTargetType targetType = TaskTargetType.STUDY;

    @Column(name = "target_id")
    private Long targetId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "completed_by")
    private Integer completedBy;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "cancelled_by")
    private Integer cancelledBy;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "last_reminder_date")
    private LocalDateTime lastReminderDate;

    @Column(name = "reminder_count", nullable = false)
    private Integer reminderCount = 0;

    @PrePersist
    void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (status == null) {
            status = TaskStatus.PENDING;
        }
        if (targetType == null) {
            targetType = TaskTargetType.STUDY;
        }
        if (reminderCount == null) {
            reminderCount = 0;
        }
    }

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
