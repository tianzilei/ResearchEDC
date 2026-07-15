package org.researchedc.module.fhir.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.researchedc.module.fhir.enums.FhirImportStatus;

@Entity(name = "ModuleFhirImportRecord")
@Table(name = "module_fhir_import_record")
public class FhirImportRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "connector_id", nullable = false)
    private Long connectorId;

    @Column(name = "study_id", nullable = false)
    private Integer studyId;

    @Column(name = "resource_type", nullable = false, length = 60)
    private String resourceType;

    @Column(name = "external_id", length = 160)
    private String externalId;

    @Column(name = "mapped_subject_identifier", length = 80)
    private String mappedSubjectIdentifier;

    @Column(name = "mapped_gender", length = 20)
    private String mappedGender;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private FhirImportStatus status = FhirImportStatus.RECEIVED;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @Column(name = "review_notes", length = 2000)
    private String reviewNotes;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    void onCreate() {
        if (createdDate == null) createdDate = LocalDateTime.now();
        if (status == null) status = FhirImportStatus.RECEIVED;
    }

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
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
