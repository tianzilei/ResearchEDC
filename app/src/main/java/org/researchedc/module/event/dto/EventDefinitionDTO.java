package org.researchedc.module.event.dto;

import java.time.LocalDateTime;

public class EventDefinitionDTO {
    private Integer studyEventDefinitionId;
    private Integer studyId;
    private String name;
    private String description;
    private Boolean repeating;
    private String type;
    private String category;
    private String ocOid;
    private Integer ordinal;
    private Integer statusId;
    private LocalDateTime dateCreated;

    public Integer getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public void setStudyEventDefinitionId(Integer v) { this.studyEventDefinitionId = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Boolean getRepeating() { return repeating; }
    public void setRepeating(Boolean v) { this.repeating = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer v) { this.ordinal = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
}
