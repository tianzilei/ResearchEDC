package org.researchedc.module.sdv.dto;

import java.time.LocalDateTime;
import org.researchedc.module.sdv.enums.SdvStatus;

public class SdvReviewDTO {
    private Long id;
    private Integer studyId;
    private Integer eventCrfId;
    private Integer studySubjectId;
    private SdvStatus status;
    private String reviewNotes;
    private Integer reviewedBy;
    private LocalDateTime reviewedDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getEventCrfId() { return eventCrfId; }
    public void setEventCrfId(Integer eventCrfId) { this.eventCrfId = eventCrfId; }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

    public SdvStatus getStatus() { return status; }
    public void setStatus(SdvStatus status) { this.status = status; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedDate() { return reviewedDate; }
    public void setReviewedDate(LocalDateTime reviewedDate) { this.reviewedDate = reviewedDate; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
