package org.researchedc.module.subjectgroup.dto;

public class SubjectGroupDTO {
    private Integer groupId;
    private String name;
    private String description;
    private Integer groupClassId;

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer v) { this.groupId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Integer getGroupClassId() { return groupClassId; }
    public void setGroupClassId(Integer v) { this.groupClassId = v; }
}
