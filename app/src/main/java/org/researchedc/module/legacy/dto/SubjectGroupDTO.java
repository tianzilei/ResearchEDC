package org.researchedc.module.legacy.dto;

public class SubjectGroupDTO {
    private int groupId;
    private String name;
    private String description;
    private int groupClassId;
    private int subjectCount;

    public int getGroupId() { return groupId; }
    public void setGroupId(int v) { this.groupId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public int getGroupClassId() { return groupClassId; }
    public void setGroupClassId(int v) { this.groupClassId = v; }
    public int getSubjectCount() { return subjectCount; }
    public void setSubjectCount(int v) { this.subjectCount = v; }
}
