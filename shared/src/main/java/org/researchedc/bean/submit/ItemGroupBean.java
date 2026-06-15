package org.researchedc.bean.submit;

import java.io.Serializable;
import java.util.ArrayList;

import org.researchedc.bean.core.AuditableEntityBean;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: May 7, 2007
 */
public class ItemGroupBean extends AuditableEntityBean implements Serializable{

    private Integer crfId = 0;
    private ItemGroupMetadataBean meta = new ItemGroupMetadataBean();
    private ArrayList itemGroupMetaBeans = new ArrayList();
    // change 07-08-07, tbh
    private ArrayList items = new ArrayList();
    private String oid;
    
    public ItemGroupBean() {
        super();
        crfId = 0;
        name = "";
        meta = new ItemGroupMetadataBean();
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

    /**
     * @return the meta
     */
    public ItemGroupMetadataBean getMeta() {
        return meta;
    }

    /**
     * @param meta
     *            the meta to set
     */
    public void setMeta(ItemGroupMetadataBean meta) {
        this.meta = meta;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public ArrayList getItemGroupMetaBeans() {
        return itemGroupMetaBeans;
    }

    public void setItemGroupMetaBeans(ArrayList itemGroupMetaBeans) {
        this.itemGroupMetaBeans = itemGroupMetaBeans;
    }

	public ArrayList getItems() {
		return items;
	}

	public void setItems(ArrayList items) {
		this.items = items;
	}
    
}
