package org.researchedc.module.ecoa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity(name = "ModuleEcoaSchedule")
@Table(name = "ecoa_schedule")
public class EcoaSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_id", nullable = false)
    private Integer studyId;

    @Column(name = "study_subject_id", nullable = false)
    private Integer studySubjectId;

    @Column(name = "study_event_id")
    private Integer studyEventId;

    @Column(name = "questionnaire_version_id", nullable = false, length = 80)
    private String questionnaireVersionId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "window_opens_at")
    private LocalDateTime windowOpensAt;

    @Column(name = "window_closes_at")
    private LocalDateTime windowClosesAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
