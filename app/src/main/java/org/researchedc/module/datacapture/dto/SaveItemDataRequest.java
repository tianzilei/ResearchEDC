package org.researchedc.module.datacapture.dto;

import jakarta.validation.constraints.NotNull;

public class SaveItemDataRequest {

    @NotNull
    private Integer eventCrfId;

    @NotNull
    private Integer itemId;

    private String value;

    private Integer statusId;

    private Integer ordinal;

    public Integer getEventCrfId() { return eventCrfId; }
    public void setEventCrfId(Integer v) { this.eventCrfId = v; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer v) { this.itemId = v; }
    public String getValue() { return value; }
    public void setValue(String v) { this.value = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer v) { this.ordinal = v; }
}
