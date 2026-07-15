package org.researchedc.module.recruit.dto;

import org.researchedc.module.recruit.enums.EligibilityDecision;

public class RecordPrescreenRequest {
    private EligibilityDecision decision;
    private Integer score;
    private String criteriaSummary;
    private String reviewNotes;

    public EligibilityDecision getDecision() { return decision; }
    public void setDecision(EligibilityDecision decision) { this.decision = decision; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getCriteriaSummary() { return criteriaSummary; }
    public void setCriteriaSummary(String criteriaSummary) { this.criteriaSummary = criteriaSummary; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
}
