package org.researchedc.module.studybuild.dto;

import java.util.Map;

public class CreateStudyTemplateRequest {
    private String name;
    private String description;
    private String category;
    private String protocolType;
    private String phase;
    private Map<String, Object> defaults;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getProtocolType() { return protocolType; }
    public void setProtocolType(String protocolType) { this.protocolType = protocolType; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public Map<String, Object> getDefaults() { return defaults; }
    public void setDefaults(Map<String, Object> defaults) { this.defaults = defaults; }
}
