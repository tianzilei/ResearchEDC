package org.researchedc.bean.submit;

import org.researchedc.bean.core.AuditableEntityBean;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: May 7, 2007
 */
public class ItemGroupBean extends AuditableEntityBean {

    private Integer crfId = 0;
    private String oid;
    
    public ItemGroupBean() {
        super();
        crfId = 0;
        name = "";
    }

    /**
     * @return the crfId
     */
    public Integer getCrfId() {
        return crfId;
    }

    /**
     * @param crfId
     *            the crfId to set
     */
    public void setCrfId(Integer crfId) {
        this.crfId = crfId;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

}
