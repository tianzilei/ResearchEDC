package org.researchedc.module.ecoa.dto;

import java.time.LocalDateTime;

public class CreateEcoaScheduleRequest {
    private Integer studyId;
    private Integer studySubjectId;
    private Integer studyEventId;
    private String questionnaireVersionId;
    private String title;
    private String description;
    private LocalDateTime dueAt;
    private LocalDateTime windowOpensAt;
    private LocalDateTime windowClosesAt;

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

    public Integer getStudyEventId() { return studyEventId; }
    public void setStudyEventId(Integer studyEventId) { this.studyEventId = studyEventId; }

    public String getQuestionnaireVersionId() { return questionnaireVersionId; }
    public void setQuestionnaireVersionId(String questionnaireVersionId) { this.questionnaireVersionId = questionnaireVersionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    public LocalDateTime getWindowOpensAt() { return windowOpensAt; }
    public void setWindowOpensAt(LocalDateTime windowOpensAt) { this.windowOpensAt = windowOpensAt; }

    public LocalDateTime getWindowClosesAt() { return windowClosesAt; }
    public void setWindowClosesAt(LocalDateTime windowClosesAt) { this.windowClosesAt = windowClosesAt; }
}
