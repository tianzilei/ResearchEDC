package org.researchedc.module.fhir.dto;

import org.researchedc.module.fhir.enums.FhirImportStatus;

public class ReconcileFhirRecordRequest {
    private FhirImportStatus status;
    private String reviewNotes;

    public FhirImportStatus getStatus() { return status; }
    public void setStatus(FhirImportStatus status) { this.status = status; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
}
