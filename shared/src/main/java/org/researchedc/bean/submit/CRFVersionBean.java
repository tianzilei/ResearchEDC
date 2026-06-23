/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.submit;

import org.researchedc.bean.core.AuditableEntityBean;

/**
 * The object to carry CRF versions in the application.
 * 
 * @author thickerson
 * 
 */
public class CRFVersionBean extends AuditableEntityBean {

    private String description = "";
    private int crfId = 0;
    private int statusId = 1;
    private String revisionNotes = "";

    private String oid;

    private String xform;
    private String xformName;

    public CRFVersionBean() {
    }

    /**
     * @return Returns the cRFId.
     */
    public int getCrfId() {
        return crfId;
    }

    /**
     * @param id
     *            The cRFId to set.
     */
    public void setCrfId(int id) {
        crfId = id;
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
     * @return Returns the revisionNotes.
     */
    public String getRevisionNotes() {
        return revisionNotes;
    }

    /**
     * @param revisionNotes
     *            The revisionNotes to set.
     */

    public void setRevisionNotes(String revisionNotes) {
        this.revisionNotes = revisionNotes;

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

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getXform() {
        return xform;
    }

    public void setXform(String xform) {
        this.xform = xform;
    }

    public String getXformName() {
        return xformName;
    }

    public void setXformName(String xformName) {
        this.xformName = xformName;
    }

}
