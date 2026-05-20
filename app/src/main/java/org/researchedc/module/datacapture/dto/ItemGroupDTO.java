package org.researchedc.module.datacapture.dto;

public class ItemGroupDTO {
    private Integer itemGroupId;
    private Integer crfId;
    private String name;
    private String ocOid;

    public Integer getItemGroupId() { return itemGroupId; }
    public void setItemGroupId(Integer v) { this.itemGroupId = v; }
    public Integer getCrfId() { return crfId; }
    public void setCrfId(Integer v) { this.crfId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
}
