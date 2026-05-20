package org.researchedc.module.legacy.dto;

public class CreateDiscrepancyNoteRequest {
    private String description;
    private String detailedNotes;
    private String entityType;
    private int entityId;
    private int studyId;
    private int eventCrfId;
    private int itemDataId;

    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getDetailedNotes() { return detailedNotes; }
    public void setDetailedNotes(String v) { this.detailedNotes = v; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String v) { this.entityType = v; }
    public int getEntityId() { return entityId; }
    public void setEntityId(int v) { this.entityId = v; }
    public int getStudyId() { return studyId; }
    public void setStudyId(int v) { this.studyId = v; }
    public int getEventCrfId() { return eventCrfId; }
    public void setEventCrfId(int v) { this.eventCrfId = v; }
    public int getItemDataId() { return itemDataId; }
    public void setItemDataId(int v) { this.itemDataId = v; }
}
