package org.akaza.openclinica.module.datacapture.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleItemData")
@Table(name = "item_data")
public class ItemDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @SequenceGenerator(name = "id_seq", sequenceName = "item_data_item_data_id_seq", allocationSize = 1)
    @Column(name = "item_data_id")
    private Integer itemDataId;

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "event_crf_id")
    private Integer eventCrfId;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Column
    private Integer ordinal;

    @Column(name = "status_id")
    private Integer statusId;

    @Column
    private Boolean deleted;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "update_id")
    private Integer updateId;

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
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
}
