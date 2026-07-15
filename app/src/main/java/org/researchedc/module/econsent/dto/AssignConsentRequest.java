package org.researchedc.module.econsent.dto;

import java.time.LocalDateTime;

public class AssignConsentRequest {
    private Integer studySubjectId;
    private Long consentVersionId;
    private LocalDateTime dueAt;

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

    public Long getConsentVersionId() { return consentVersionId; }
    public void setConsentVersionId(Long consentVersionId) { this.consentVersionId = consentVersionId; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
}
