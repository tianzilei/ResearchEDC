package org.researchedc.module.discrepancynote.dto;

public class CreateDiscrepancyNoteRequest {
    private String description;
    private String detailedNotes;
    private String entityType;
    private Integer entityId;
    private Integer studyId;

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
}
