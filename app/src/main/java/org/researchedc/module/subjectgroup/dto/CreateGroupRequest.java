package org.researchedc.module.subjectgroup.dto;

public class CreateGroupRequest {
    private String name;
    private String description;

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
}
