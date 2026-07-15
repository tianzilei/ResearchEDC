package org.researchedc.module.studybuild.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class StudyTemplateDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String protocolType;
    private String phase;
    private boolean active;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Map<String, Object> defaults;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public Map<String, Object> getDefaults() { return defaults; }
    public void setDefaults(Map<String, Object> defaults) { this.defaults = defaults; }
}
