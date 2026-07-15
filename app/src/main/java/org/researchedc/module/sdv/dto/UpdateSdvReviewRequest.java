package org.researchedc.module.sdv.dto;

import org.researchedc.module.sdv.enums.SdvStatus;

public class UpdateSdvReviewRequest {
    private SdvStatus status;
    private String reviewNotes;

    public SdvStatus getStatus() { return status; }
    public void setStatus(SdvStatus status) { this.status = status; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
}
