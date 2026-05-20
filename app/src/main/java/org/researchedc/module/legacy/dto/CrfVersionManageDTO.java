package org.researchedc.module.legacy.dto;

import java.util.Date;

public class CrfVersionManageDTO {
    private int crfVersionId;
    private int crfId;
    private String name;
    private String description;
    private String revisionNotes;
    private String status;
    private Date dateCreated;

    public int getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(int v) { this.crfVersionId = v; }
    public int getCrfId() { return crfId; }
    public void setCrfId(int v) { this.crfId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getRevisionNotes() { return revisionNotes; }
    public void setRevisionNotes(String v) { this.revisionNotes = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date v) { this.dateCreated = v; }
}
