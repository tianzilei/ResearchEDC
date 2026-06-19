package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;
import java.util.List;

public class ItemGroupCrfVersionView {

    private String itemName;
    private String groupName;
    private String groupOID;
    private String crfVersionName;
    private int crfVersionStatus;
    private String itemOID;
    private String itemDescription;
    private String itemDataType;
    private String versions;
    private String errorMesages;
    private List arrErrorMesages;
    private int id;

    public ItemGroupCrfVersionView() {
        arrErrorMesages = new ArrayList<String>();
    }

    public ItemGroupCrfVersionView(String itemName, String groupName, String groupOID,
                                   String crfVersionName, int crfVersionStatus) {
        this.itemName = itemName;
        this.groupName = groupName;
        this.groupOID = groupOID;
        this.crfVersionName = crfVersionName;
        this.crfVersionStatus = crfVersionStatus;
        arrErrorMesages = new ArrayList<String>();
    }

    public ItemGroupCrfVersionView(String itemName, String groupName, String groupOID,
                                   String crfVersionName, int crfVersionStatus,
                                   String itemOID, String itemDescription, String itemDataType, int id) {
        this(itemName, groupName, groupOID, crfVersionName, crfVersionStatus);
        this.itemOID = itemOID;
        this.itemDescription = itemDescription;
        this.itemDataType = itemDataType;
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupOID() {
        return groupOID;
    }

    public void setGroupOID(String groupOID) {
        this.groupOID = groupOID;
    }

    public String getCrfVersionName() {
        return crfVersionName;
    }

    public void setCrfVersionName(String crfVersionName) {
        this.crfVersionName = crfVersionName;
    }

    public int getCrfVersionStatus() {
        return crfVersionStatus;
    }

    public void setCrfVersionStatus(int crfVersionStatus) {
        this.crfVersionStatus = crfVersionStatus;
    }

    public String getItemOID() {
        return itemOID;
    }

    public void setItemOID(String itemOID) {
        this.itemOID = itemOID;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemDataType() {
        return itemDataType;
    }

    public void setItemDataType(String itemDataType) {
        this.itemDataType = itemDataType;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }

    public String getErrorMesages() {
        return errorMesages;
    }

    public void setErrorMesages(String errorMesages) {
        this.errorMesages = errorMesages;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List getArrErrorMesages() {
        return arrErrorMesages;
    }

    public void setArrErrorMesages(List arrErrorMesages) {
        this.arrErrorMesages = arrErrorMesages;
    }
}
