package org.researchedc.module.filter.dto;

import java.time.LocalDateTime;

public class FilterDTO {
    private Integer filterId;
    private String name;
    private String description;
    private Integer ownerId;
    private LocalDateTime dateCreated;

    public Integer getFilterId() { return filterId; }
    public void setFilterId(Integer v) { this.filterId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
}
