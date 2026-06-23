/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.submit;

import org.researchedc.bean.core.AuditableEntityBean;

/**
 * <P>
 * ItemBean.java.
 *
 * @author thickerson
 */
public class ItemBean extends AuditableEntityBean {
    private static final int ITEM_DATA_TYPE_ST = 5;

    private String description = "";

    private String units = "";

    private boolean phiStatus = false;

    private int itemDataTypeId = ITEM_DATA_TYPE_ST;

    private int itemReferenceTypeId = 0;

    private int statusId = 1;

    private String oid;

    public ItemBean() {
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the itemDataTypeId.
     */
    public int getItemDataTypeId() {
        return itemDataTypeId;
    }

    /**
     * @param itemDataTypeId
     *            The itemDataTypeId to set.
     */
    public void setItemDataTypeId(int itemDataTypeId) {
        this.itemDataTypeId = itemDataTypeId;
    }

    /**
     * @return Returns the itemReferenceTypeId.
     */
    public int getItemReferenceTypeId() {
        return itemReferenceTypeId;
    }

    /**
     * @param itemReferenceTypeId
     *            The itemReferenceTypeId to set.
     */
    public void setItemReferenceTypeId(int itemReferenceTypeId) {
        this.itemReferenceTypeId = itemReferenceTypeId;
    }

    /**
     * @return Returns the phiStatus.
     */
    public boolean isPhiStatus() {
        return phiStatus;
    }

    /**
     * @param phiStatus
     *            The phiStatus to set.
     */
    public void setPhiStatus(boolean phiStatus) {
        this.phiStatus = phiStatus;
    }

    /**
     * @return Returns the statusId.
     */
    public int getStatusId() {
        return statusId;
    }

    /**
     * @param statusId
     *            The statusId to set.
     */
    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    /**
     * @return Returns the units.
     */
    public String getUnits() {
        return units;
    }

    /**
     * @param units
     *            The units to set.
     */
    public void setUnits(String units) {
        this.units = units;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

}
