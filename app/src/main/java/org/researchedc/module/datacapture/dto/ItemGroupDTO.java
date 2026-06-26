package org.researchedc.module.datacapture.dto;

import org.researchedc.app.dto.AuditableEntity;

public class ItemGroupDTO extends AuditableEntity {
    private Integer itemGroupId;
    private Integer crfId;
    private String oid;
    private String ocOid;
    private java.util.List<Integer> items;

    public ItemGroupDTO() {
        super();
        crfId = 0;
        name = "";
    }

    public Integer getItemGroupId() {
        return itemGroupId;
    }

    public void setItemGroupId(Integer itemGroupId) {
        this.itemGroupId = itemGroupId;
    }

    public Integer getCrfId() {
        return crfId;
    }

    public void setCrfId(Integer crfId) {
        this.crfId = crfId;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOcOid() {
        return ocOid;
    }

    public void setOcOid(String ocOid) {
        this.ocOid = ocOid;
    }

    public java.util.List<Integer> getItems() {
        return items;
    }

    public void setItems(java.util.List<Integer> items) {
        this.items = items;
    }
}
