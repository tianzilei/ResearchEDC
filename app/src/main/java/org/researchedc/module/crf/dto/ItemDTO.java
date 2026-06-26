package org.researchedc.module.crf.dto;

import org.researchedc.app.dto.AuditableEntity;

public class ItemDTO extends AuditableEntity {
    private Integer itemId;
    private String description;
    private String units;
    private boolean phiStatus;
    private int itemDataTypeId;
    private int itemReferenceTypeId;
    private int statusId;
    private String oid;
    private String ocOid;
    private String dataType;
    private boolean phi;
    private int ordinal;
    private String defaultValue;
    private boolean required;
    private String regexp;
    private String regexpErrorMsg;

    public ItemDTO() {
        description = "";
        units = "";
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getItemDataTypeId() {
        return itemDataTypeId;
    }

    public void setItemDataTypeId(int itemDataTypeId) {
        this.itemDataTypeId = itemDataTypeId;
    }

    public int getItemReferenceTypeId() {
        return itemReferenceTypeId;
    }

    public void setItemReferenceTypeId(int itemReferenceTypeId) {
        this.itemReferenceTypeId = itemReferenceTypeId;
    }

    public boolean isPhiStatus() {
        return phiStatus;
    }

    public void setPhiStatus(boolean phiStatus) {
        this.phiStatus = phiStatus;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isPhi() {
        return phi;
    }

    public void setPhi(boolean phi) {
        this.phi = phi;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public String getRegexpErrorMsg() {
        return regexpErrorMsg;
    }

    public void setRegexpErrorMsg(String regexpErrorMsg) {
        this.regexpErrorMsg = regexpErrorMsg;
    }
}
