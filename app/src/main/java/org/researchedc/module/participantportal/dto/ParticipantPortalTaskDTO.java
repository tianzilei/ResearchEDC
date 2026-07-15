package org.researchedc.module.participantportal.dto;

import java.time.LocalDateTime;

public class ParticipantPortalTaskDTO {
    private String id;
    private String type;
    private Long assignmentId;
    private Long taskInstanceId;
    private String title;
    private String subtitle;
    private String description;
    private String status;
    private LocalDateTime dueAt;
    private boolean actionable;
    private String questionnaireAssignmentId;
    private String consentVersionLabel;
    private String consentBodyText;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Long getTaskInstanceId() { return taskInstanceId; }
    public void setTaskInstanceId(Long taskInstanceId) { this.taskInstanceId = taskInstanceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    public boolean isActionable() { return actionable; }
    public void setActionable(boolean actionable) { this.actionable = actionable; }

    public String getQuestionnaireAssignmentId() { return questionnaireAssignmentId; }
    public void setQuestionnaireAssignmentId(String questionnaireAssignmentId) {
        this.questionnaireAssignmentId = questionnaireAssignmentId;
    }

    public String getConsentVersionLabel() { return consentVersionLabel; }
    public void setConsentVersionLabel(String consentVersionLabel) { this.consentVersionLabel = consentVersionLabel; }

    public String getConsentBodyText() { return consentBodyText; }
    public void setConsentBodyText(String consentBodyText) { this.consentBodyText = consentBodyText; }
}
