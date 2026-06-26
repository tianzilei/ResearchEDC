package org.researchedc.module.datacapture.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleItemGroup")
@Table(name = "module_item_group")
public class ItemGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ig_seq")
    @SequenceGenerator(name = "ig_seq", sequenceName = "module_item_group_id_seq", allocationSize = 1)
    @Column(name = "item_group_id")
    private Integer itemGroupId;

    @Column(name = "crf_id")
    private Integer crfId;

    @Column(length = 255)
    private String name;

    @Column(name = "oc_oid", length = 40)
    private String ocOid;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "update_id")
    private Integer updateId;

    public Integer getItemGroupId() { return itemGroupId; }
    public void setItemGroupId(Integer v) { this.itemGroupId = v; }
    public Integer getCrfId() { return crfId; }
    public void setCrfId(Integer v) { this.crfId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
}
