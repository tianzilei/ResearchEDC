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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + itemDataTypeId;
        result = prime * result + itemReferenceTypeId;
        result = prime * result + ((oid == null) ? 0 : oid.hashCode());
        result = prime * result + (phiStatus ? 1231 : 1237);
        result = prime * result + statusId;
        result = prime * result + ((units == null) ? 0 : units.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ItemBean other = (ItemBean) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (itemDataTypeId != other.itemDataTypeId)
            return false;
        if (itemReferenceTypeId != other.itemReferenceTypeId)
            return false;
        if (oid == null) {
            if (other.oid != null)
                return false;
        } else if (!oid.equals(other.oid))
            return false;
        if (phiStatus != other.phiStatus)
            return false;
        if (statusId != other.statusId)
            return false;
        if (units == null) {
            if (other.units != null)
                return false;
        } else if (!units.equals(other.units))
            return false;
        return true;
    }
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
