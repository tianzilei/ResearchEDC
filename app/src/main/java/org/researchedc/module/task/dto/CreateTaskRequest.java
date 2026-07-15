package org.researchedc.module.task.dto;

import java.time.LocalDateTime;
import org.researchedc.module.task.enums.TaskTargetType;

public class CreateTaskRequest {
    private Long templateId;
    private Integer studyId;
    private Integer assignedTo;
    private String title;
    private String description;
    private TaskTargetType targetType;
    private Long targetId;
    private LocalDateTime dueDate;

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

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
}
