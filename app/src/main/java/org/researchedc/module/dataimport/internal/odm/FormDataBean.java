package org.researchedc.module.dataimport.internal.odm;

import java.util.ArrayList;

public class FormDataBean {
    private ArrayList<ImportItemGroupDataBean> itemGroupData;
    private String formOID;
    private String EventCRFStatus;

    public FormDataBean() {
        itemGroupData = new ArrayList<ImportItemGroupDataBean>();
    }

    public String getFormOID() {
        return formOID;
    }

    public void setFormOID(String formOID) {
        this.formOID = formOID;
    }

    public String getEventCRFStatus() {
        return EventCRFStatus;
    }

    public void setEventCRFStatus(String eventCRFStatus) {
        EventCRFStatus = eventCRFStatus;
    }

    public ArrayList<ImportItemGroupDataBean> getItemGroupData() {
        return itemGroupData;
    }

    public void setItemGroupData(ArrayList<ImportItemGroupDataBean> itemGroupData) {
        this.itemGroupData = itemGroupData;
    }
}
