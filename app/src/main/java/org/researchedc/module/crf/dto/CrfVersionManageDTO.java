package org.researchedc.module.crf.dto;

import java.time.LocalDateTime;

public class CrfVersionManageDTO {
    private Integer crfVersionId;
    private Integer crfId;
    private String name;
    private String description;
    private String revisionNotes;
    private Integer statusId;
    private LocalDateTime dateCreated;

    public Integer getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(Integer v) { this.crfVersionId = v; }
    public Integer getCrfId() { return crfId; }
    public void setCrfId(Integer v) { this.crfId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getRevisionNotes() { return revisionNotes; }
    public void setRevisionNotes(String v) { this.revisionNotes = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
}
