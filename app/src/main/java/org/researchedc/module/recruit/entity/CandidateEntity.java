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
import org.researchedc.module.recruit.enums.CandidateStatus;

@Entity(name = "ModuleRecruitCandidate")
@Table(name = "module_recruit_candidate")
public class CandidateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_id", nullable = false)
    private Integer studyId;

    @Column(name = "candidate_code", nullable = false, length = 80)
    private String candidateCode;

    @Column(name = "display_name", length = 160)
    private String displayName;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 80)
    private String contactPhone;

    @Column(name = "source", length = 120)
    private String source;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private CandidateStatus status = CandidateStatus.NEW;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "converted_subject_id")
    private Integer convertedSubjectId;

    @Column(name = "converted_study_subject_id")
    private Integer convertedStudySubjectId;

    @PrePersist
    void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (status == null) {
            status = CandidateStatus.NEW;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public String getCandidateCode() { return candidateCode; }
    public void setCandidateCode(String candidateCode) { this.candidateCode = candidateCode; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public CandidateStatus getStatus() { return status; }
    public void setStatus(CandidateStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public Integer getConvertedSubjectId() { return convertedSubjectId; }
    public void setConvertedSubjectId(Integer convertedSubjectId) { this.convertedSubjectId = convertedSubjectId; }

    public Integer getConvertedStudySubjectId() { return convertedStudySubjectId; }
    public void setConvertedStudySubjectId(Integer convertedStudySubjectId) {
        this.convertedStudySubjectId = convertedStudySubjectId;
    }
}
