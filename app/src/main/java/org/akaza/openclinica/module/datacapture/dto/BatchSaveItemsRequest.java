package org.akaza.openclinica.module.datacapture.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BatchSaveItemsRequest {

    @NotNull
    private Integer eventCrfId;

    @NotEmpty
    @Valid
    private List<SaveItemDataRequest> items;

    public Integer getEventCrfId() { return eventCrfId; }
    public void setEventCrfId(Integer v) { this.eventCrfId = v; }
    public List<SaveItemDataRequest> getItems() { return items; }
    public void setItems(List<SaveItemDataRequest> v) { this.items = v; }
}
