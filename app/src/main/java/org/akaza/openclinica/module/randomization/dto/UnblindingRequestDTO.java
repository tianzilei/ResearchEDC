package org.akaza.openclinica.module.randomization.dto;

import java.time.LocalDateTime;
import org.akaza.openclinica.module.randomization.enums.UnblindingStatus;

public class UnblindingRequestDTO {

    private Long id;
    private Long assignmentId;
    private String armName;
    private String subjectKey;
    private Integer requestedBy;
    private LocalDateTime requestedDate;
    private String reason;
    private UnblindingStatus status;
    private Integer reviewedBy;
    private LocalDateTime reviewedDate;
    private String reviewNotes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public String getArmName() { return armName; }
    public void setArmName(String armName) { this.armName = armName; }

    public String getSubjectKey() { return subjectKey; }
    public void setSubjectKey(String subjectKey) { this.subjectKey = subjectKey; }

    public Integer getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Integer requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public UnblindingStatus getStatus() { return status; }
    public void setStatus(UnblindingStatus status) { this.status = status; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedDate() { return reviewedDate; }
    public void setReviewedDate(LocalDateTime reviewedDate) { this.reviewedDate = reviewedDate; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
}
