package org.researchedc.module.legacy.dto;

import java.util.Date;

public class DatasetDTO {
    private int datasetId;
    private String name;
    private String description;
    private int studyId;
    private String studyName;
    private int ownerId;
    private String status;
    private Date dateCreated;

    public int getDatasetId() { return datasetId; }
    public void setDatasetId(int v) { this.datasetId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public int getStudyId() { return studyId; }
    public void setStudyId(int v) { this.studyId = v; }
    public String getStudyName() { return studyName; }
    public void setStudyName(String v) { this.studyName = v; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int v) { this.ownerId = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date v) { this.dateCreated = v; }
}
