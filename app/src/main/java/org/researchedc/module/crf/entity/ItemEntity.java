package org.researchedc.module.crf.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleItem")
@Table(name = "module_item")
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_item_seq")
    @SequenceGenerator(name = "module_item_seq", sequenceName = "module_item_id_seq", allocationSize = 1)
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "units")
    private String units;

    @Column(name = "item_data_type_id")
    private Integer itemDataTypeId;

    @Column(name = "oc_oid", length = 40)
    private String ocOid;

    @Column(name = "phi_status")
    private Boolean phiStatus;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "update_id")
    private Integer updateId;

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer v) { this.itemId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getUnits() { return units; }
    public void setUnits(String v) { this.units = v; }
    public Integer getItemDataTypeId() { return itemDataTypeId; }
    public void setItemDataTypeId(Integer v) { this.itemDataTypeId = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public Boolean getPhiStatus() { return phiStatus; }
    public void setPhiStatus(Boolean v) { this.phiStatus = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
}
