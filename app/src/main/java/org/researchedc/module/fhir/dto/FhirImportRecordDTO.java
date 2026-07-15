package org.researchedc.module.fhir.dto;

import java.time.LocalDateTime;
import org.researchedc.module.fhir.enums.FhirImportStatus;

public class FhirImportRecordDTO {
    private Long id;
    private Long connectorId;
    private Integer studyId;
    private String resourceType;
    private String externalId;
    private String mappedSubjectIdentifier;
    private String mappedGender;
    private FhirImportStatus status;
    private String reviewNotes;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConnectorId() { return connectorId; }
    public void setConnectorId(Long connectorId) { this.connectorId = connectorId; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getMappedSubjectIdentifier() { return mappedSubjectIdentifier; }
    public void setMappedSubjectIdentifier(String mappedSubjectIdentifier) { this.mappedSubjectIdentifier = mappedSubjectIdentifier; }
    public String getMappedGender() { return mappedGender; }
    public void setMappedGender(String mappedGender) { this.mappedGender = mappedGender; }
    public FhirImportStatus getStatus() { return status; }
    public void setStatus(FhirImportStatus status) { this.status = status; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
