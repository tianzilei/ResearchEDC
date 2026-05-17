package org.akaza.openclinica.module.crf.dto;

public class ItemDTO {
    private int itemId;
    private String name;
    private String description;
    private String units;
    private String dataType;
    private String ocOid;
    private String responseType;
    private boolean phi;
    private int ordinal;
    private String defaultValue;
    private boolean required;
    private String regexp;
    private String regexpErrorMsg;

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUnits() { return units; }
    public void setUnits(String units) { this.units = units; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String ocOid) { this.ocOid = ocOid; }
    public String getResponseType() { return responseType; }
    public void setResponseType(String responseType) { this.responseType = responseType; }
    public boolean isPhi() { return phi; }
    public void setPhi(boolean phi) { this.phi = phi; }
    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public String getRegexp() { return regexp; }
    public void setRegexp(String regexp) { this.regexp = regexp; }
    public String getRegexpErrorMsg() { return regexpErrorMsg; }
    public void setRegexpErrorMsg(String regexpErrorMsg) { this.regexpErrorMsg = regexpErrorMsg; }
}
