package org.researchedc.module.ecoa.dto;

import java.time.LocalDateTime;
import org.researchedc.module.ecoa.enums.EcoaAssignmentStatus;

public class EcoaAssignmentDTO {
    private Long id;
    private Long scheduleId;
    private Integer studyId;
    private Integer studySubjectId;
    private Long participantAccountId;
    private Long participantTokenId;
    private Long taskInstanceId;
    private String questionnaireAssignmentId;
    private EcoaAssignmentStatus status;
    private LocalDateTime dueAt;
    private LocalDateTime windowOpensAt;
    private LocalDateTime windowClosesAt;
    private String entryUrl;
    private LocalDateTime completedAt;
    private String scoreSummary;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

    public Long getParticipantAccountId() { return participantAccountId; }
    public void setParticipantAccountId(Long participantAccountId) { this.participantAccountId = participantAccountId; }

    public Long getParticipantTokenId() { return participantTokenId; }
    public void setParticipantTokenId(Long participantTokenId) { this.participantTokenId = participantTokenId; }

    public Long getTaskInstanceId() { return taskInstanceId; }
    public void setTaskInstanceId(Long taskInstanceId) { this.taskInstanceId = taskInstanceId; }

    public String getQuestionnaireAssignmentId() { return questionnaireAssignmentId; }
    public void setQuestionnaireAssignmentId(String questionnaireAssignmentId) { this.questionnaireAssignmentId = questionnaireAssignmentId; }

    public EcoaAssignmentStatus getStatus() { return status; }
    public void setStatus(EcoaAssignmentStatus status) { this.status = status; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    public LocalDateTime getWindowOpensAt() { return windowOpensAt; }
    public void setWindowOpensAt(LocalDateTime windowOpensAt) { this.windowOpensAt = windowOpensAt; }

    public LocalDateTime getWindowClosesAt() { return windowClosesAt; }
    public void setWindowClosesAt(LocalDateTime windowClosesAt) { this.windowClosesAt = windowClosesAt; }

    public String getEntryUrl() { return entryUrl; }
    public void setEntryUrl(String entryUrl) { this.entryUrl = entryUrl; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getScoreSummary() { return scoreSummary; }
    public void setScoreSummary(String scoreSummary) { this.scoreSummary = scoreSummary; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
