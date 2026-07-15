package org.researchedc.module.ecoa.dto;

import java.time.LocalDateTime;

public class RecordEcoaCompletionRequest {
    private String questionnaireAssignmentId;
    private LocalDateTime completedAt;
    private String scoreSummary;

    public String getQuestionnaireAssignmentId() { return questionnaireAssignmentId; }
    public void setQuestionnaireAssignmentId(String questionnaireAssignmentId) { this.questionnaireAssignmentId = questionnaireAssignmentId; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getScoreSummary() { return scoreSummary; }
    public void setScoreSummary(String scoreSummary) { this.scoreSummary = scoreSummary; }
}
