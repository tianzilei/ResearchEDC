package org.researchedc.module.legacy.dto;

import java.util.Date;

public class DiscrepancyNoteDTO {
    private int discrepancyNoteId;
    private String description;
    private String detailedNotes;
    private String type;
    private String resolutionStatus;
    private String entityType;
    private String column;
    private int entityId;
    private int studyId;
    private int ownerId;
    private String ownerName;
    private Date dateCreated;
    private int parentDnId;
    private boolean hasChildren;
    private int eventCRFId;
    private String subjectName;
    private String eventName;
    private String crfName;
    private String entityName;

    public int getDiscrepancyNoteId() { return discrepancyNoteId; }
    public void setDiscrepancyNoteId(int v) { this.discrepancyNoteId = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getDetailedNotes() { return detailedNotes; }
    public void setDetailedNotes(String v) { this.detailedNotes = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public String getResolutionStatus() { return resolutionStatus; }
    public void setResolutionStatus(String v) { this.resolutionStatus = v; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String v) { this.entityType = v; }
    public String getColumn() { return column; }
    public void setColumn(String v) { this.column = v; }
    public int getEntityId() { return entityId; }
    public void setEntityId(int v) { this.entityId = v; }
    public int getStudyId() { return studyId; }
    public void setStudyId(int v) { this.studyId = v; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int v) { this.ownerId = v; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String v) { this.ownerName = v; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date v) { this.dateCreated = v; }
    public int getParentDnId() { return parentDnId; }
    public void setParentDnId(int v) { this.parentDnId = v; }
    public boolean isHasChildren() { return hasChildren; }
    public void setHasChildren(boolean v) { this.hasChildren = v; }
    public int getEventCRFId() { return eventCRFId; }
    public void setEventCRFId(int v) { this.eventCRFId = v; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String v) { this.subjectName = v; }
    public String getEventName() { return eventName; }
    public void setEventName(String v) { this.eventName = v; }
    public String getCrfName() { return crfName; }
    public void setCrfName(String v) { this.crfName = v; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String v) { this.entityName = v; }
}
