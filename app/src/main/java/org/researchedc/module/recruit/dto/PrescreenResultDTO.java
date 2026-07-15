package org.researchedc.module.recruit.dto;

import java.time.LocalDateTime;
import org.researchedc.module.recruit.enums.EligibilityDecision;

public class PrescreenResultDTO {
    private Long id;
    private Long candidateId;
    private Integer studyId;
    private EligibilityDecision decision;
    private Integer score;
    private String criteriaSummary;
    private String reviewNotes;
    private Integer reviewedBy;
    private LocalDateTime reviewedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public EligibilityDecision getDecision() { return decision; }
    public void setDecision(EligibilityDecision decision) { this.decision = decision; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getCriteriaSummary() { return criteriaSummary; }
    public void setCriteriaSummary(String criteriaSummary) { this.criteriaSummary = criteriaSummary; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedDate() { return reviewedDate; }
    public void setReviewedDate(LocalDateTime reviewedDate) { this.reviewedDate = reviewedDate; }
}
