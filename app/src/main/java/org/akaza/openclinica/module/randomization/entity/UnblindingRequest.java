package org.akaza.openclinica.module.randomization.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.akaza.openclinica.module.randomization.enums.UnblindingStatus;

@Entity
@Table(name = "randomization_unblinding_request")
public class UnblindingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private RandomizationAssignment assignment;

    @Column(name = "requested_by", nullable = false)
    private Integer requestedBy;

    @Column(name = "requested_date")
    private LocalDateTime requestedDate;

    @Column(length = 2000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnblindingStatus status = UnblindingStatus.PENDING;

    @Column(name = "reviewed_by")
    private Integer reviewedBy;

    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    @Column(name = "review_notes", length = 2000)
    private String reviewNotes;

    @PrePersist
    protected void onCreate() {
        requestedDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RandomizationAssignment getAssignment() { return assignment; }
    public void setAssignment(RandomizationAssignment assignment) { this.assignment = assignment; }

    public Integer getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Integer requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getRequestedDate() { return requestedDate; }

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
