package org.researchedc.module.discrepancynote.dto;

import java.time.LocalDateTime;

public class DiscrepancyNoteDTO {
    private Integer discrepancyNoteId;
    private String description;
    private String detailedNotes;
    private String entityType;
    private Integer entityId;
    private Integer studyId;
    private Integer ownerId;
    private Integer parentDnId;
    private LocalDateTime dateCreated;
    private Integer resolutionStatusId;
    private Integer discrepancyNoteTypeId;

    public Integer getDiscrepancyNoteId() { return discrepancyNoteId; }
    public void setDiscrepancyNoteId(Integer v) { this.discrepancyNoteId = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getDetailedNotes() { return detailedNotes; }
    public void setDetailedNotes(String v) { this.detailedNotes = v; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String v) { this.entityType = v; }
    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer v) { this.entityId = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public Integer getParentDnId() { return parentDnId; }
    public void setParentDnId(Integer v) { this.parentDnId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public Integer getResolutionStatusId() { return resolutionStatusId; }
    public void setResolutionStatusId(Integer v) { this.resolutionStatusId = v; }
    public Integer getDiscrepancyNoteTypeId() { return discrepancyNoteTypeId; }
    public void setDiscrepancyNoteTypeId(Integer v) { this.discrepancyNoteTypeId = v; }
}
