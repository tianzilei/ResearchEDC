package org.researchedc.module.dataset.dto;

import java.time.LocalDateTime;

public class DatasetDTO {
    private Integer datasetId;
    private String name;
    private String description;
    private Integer studyId;
    private Integer ownerId;
    private Integer statusId;
    private LocalDateTime dateCreated;

    public Integer getDatasetId() { return datasetId; }
    public void setDatasetId(Integer v) { this.datasetId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
}
