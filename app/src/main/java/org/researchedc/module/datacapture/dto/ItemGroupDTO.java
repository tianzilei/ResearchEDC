package org.researchedc.module.datacapture.dto;

import java.util.List;

public class ItemGroupDTO {
    private Integer itemGroupId;
    private Integer crfId;
    private String name;
    private String ocOid;
    private List<Integer> items;

    public Integer getItemGroupId() { return itemGroupId; }
    public void setItemGroupId(Integer v) { this.itemGroupId = v; }
    public Integer getCrfId() { return crfId; }
    public void setCrfId(Integer v) { this.crfId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public List<Integer> getItems() { return items; }
    public void setItems(List<Integer> v) { this.items = v; }
}
