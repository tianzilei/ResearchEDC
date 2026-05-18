package org.akaza.openclinica.module.datacapture.dto;

import java.time.LocalDateTime;

public class ItemDataDTO {
    private Integer itemDataId;
    private Integer itemId;
    private Integer eventCrfId;
    private String value;
    private Integer ordinal;
    private Integer statusId;
    private Boolean deleted;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;

    public Integer getItemDataId() { return itemDataId; }
    public void setItemDataId(Integer v) { this.itemDataId = v; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer v) { this.itemId = v; }
    public Integer getEventCrfId() { return eventCrfId; }
    public void setEventCrfId(Integer v) { this.eventCrfId = v; }
    public String getValue() { return value; }
    public void setValue(String v) { this.value = v; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer v) { this.ordinal = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean v) { this.deleted = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
}
