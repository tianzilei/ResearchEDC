package org.researchedc.module.recruit.entity;

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
import org.researchedc.module.recruit.enums.EligibilityDecision;

@Entity(name = "ModulePrescreenResult")
@Table(name = "module_prescreen_result")
public class PrescreenResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "study_id", nullable = false)
    private Integer studyId;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private EligibilityDecision decision;

    @Column(name = "score")
    private Integer score;

    @Column(name = "criteria_summary", length = 2000)
    private String criteriaSummary;

    @Column(name = "review_notes", length = 2000)
    private String reviewNotes;

    @Column(name = "reviewed_by")
    private Integer reviewedBy;

    @Column(name = "reviewed_date", nullable = false)
    private LocalDateTime reviewedDate;

    @PrePersist
    void onCreate() {
        if (reviewedDate == null) {
            reviewedDate = LocalDateTime.now();
        }
    }

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
