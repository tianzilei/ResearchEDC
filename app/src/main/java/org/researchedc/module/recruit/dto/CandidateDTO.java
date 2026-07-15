package org.researchedc.module.recruit.dto;

import java.time.LocalDateTime;
import org.researchedc.module.recruit.enums.CandidateStatus;

public class CandidateDTO {
    private Long id;
    private Integer studyId;
    private String candidateCode;
    private String displayName;
    private String contactEmail;
    private String contactPhone;
    private String source;
    private CandidateStatus status;
    private String notes;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Integer convertedSubjectId;
    private Integer convertedStudySubjectId;

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
