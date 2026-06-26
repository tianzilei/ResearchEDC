package org.researchedc.module.openrosa.service;

/**
 * Simple POJO representing a single item-value pair parsed from an XForm submission.
 */
public class ItemValue {

    private String itemOid;
    private String value;
    private Integer itemDataTypeId;
    private Integer responseTypeId;

    public ItemValue() {}

    public ItemValue(String itemOid, String value) {
        this.itemOid = itemOid;
        this.value = value;
    }

    public ItemValue(String itemOid, String value, Integer itemDataTypeId, Integer responseTypeId) {
        this.itemOid = itemOid;
        this.value = value;
        this.itemDataTypeId = itemDataTypeId;
        this.responseTypeId = responseTypeId;
    }

    public String getItemOid() { return itemOid; }
    public void setItemOid(String v) { this.itemOid = v; }

    public String getValue() { return value; }
    public void setValue(String v) { this.value = v; }

    public Integer getItemDataTypeId() { return itemDataTypeId; }
    public void setItemDataTypeId(Integer v) { this.itemDataTypeId = v; }

    public Integer getResponseTypeId() { return responseTypeId; }
    public void setResponseTypeId(Integer v) { this.responseTypeId = v; }

    /**
     * Returns true if the value is non-null and non-empty.
     */
    public boolean hasValue() {
        return value != null && !value.isEmpty();
    }
}
