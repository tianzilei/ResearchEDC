package org.researchedc.module.task.dto;

import java.time.LocalDateTime;

public class TaskTemplateDTO {
    private Long id;
    private Integer studyId;
    private String name;
    private String description;
    private String taskType;
    private Integer defaultDueDays;
    private Boolean active;
    private Integer createdBy;
    private LocalDateTime createdDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public Integer getDefaultDueDays() { return defaultDueDays; }
    public void setDefaultDueDays(Integer defaultDueDays) { this.defaultDueDays = defaultDueDays; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
