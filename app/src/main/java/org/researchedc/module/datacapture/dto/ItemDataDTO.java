package org.researchedc.module.datacapture.dto;

import org.researchedc.app.dto.AuditableEntity;

public class ItemDataDTO extends AuditableEntity {
    private Integer itemDataId;
    private int eventCRFId;
    private int itemId;
    private String value;
    private int ordinal;
    private boolean deleted;
    private Integer statusId;
    private java.time.LocalDateTime dateCreated;
    private java.time.LocalDateTime dateUpdated;

    public ItemDataDTO() {
        eventCRFId = 0;
        itemId = 0;
        value = "";
        ordinal = 1;
        deleted = false;
    }

    public Integer getItemDataId() {
        return itemDataId;
    }

    public void setItemDataId(Integer itemDataId) {
        this.itemDataId = itemDataId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getEventCRFId() {
        return eventCRFId;
    }

    public void setEventCRFId(int eventCRFId) {
        this.eventCRFId = eventCRFId;
    }

    public void setEventCrfId(Integer eventCrfId) {
        this.eventCRFId = eventCrfId != null ? eventCrfId : 0;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public java.time.LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(java.time.LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public java.time.LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(java.time.LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
}
