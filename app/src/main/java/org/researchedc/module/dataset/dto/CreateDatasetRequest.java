package org.researchedc.module.dataset.dto;

public class CreateDatasetRequest {
    private String name;
    private String description;
    private Integer studyId;

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
}
