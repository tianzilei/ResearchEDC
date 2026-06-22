package org.researchedc.module.dataimport.internal.odm;

public class ImportItemDataBean {
    private String itemOID;
    private String transactionType;
    private String value;
    private String isNull; // boolean, tbh?
    private String reasonForNull;
    
    private boolean hasValueWithNull; //this is just a flag, it is not an attribute/element

    public String getItemOID() {
        return itemOID;
    }

    public void setItemOID(String itemOID) {
        this.itemOID = itemOID;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIsNull() {
        return isNull;
    }

    public void setIsNull(String isNull) {
        this.isNull = isNull;
    }

    public String getReasonForNull() {
        return reasonForNull;
    }

    public void setReasonForNull(String reasonForNull) {
        this.reasonForNull = reasonForNull;
    }

    public boolean isHasValueWithNull() {
        return hasValueWithNull;
    }

    public void setHasValueWithNull(boolean hasValueWithNull) {
        this.hasValueWithNull = hasValueWithNull;
    }
}
