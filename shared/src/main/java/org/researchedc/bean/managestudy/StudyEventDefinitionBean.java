/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.managestudy;

import org.researchedc.bean.core.AuditableEntityBean;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author thickerson
 *
 *
 */
public class StudyEventDefinitionBean extends AuditableEntityBean {
    private String description = "";

    private boolean repeating = false;

    private String category = "";

    private String type = "";

    private int studyId;// fk for study table

    private ArrayList crfs = new ArrayList();

    private int crfNum = 0; // number of crfs, not in DB

    private int ordinal = 1;

    private boolean lockable = false;// not in the DB, check whether we can
    // show
    // lock link on the JSP

    private boolean populated = false;// not in DB

    // Will be used to show CRFs and their default version in the Event
    // Definition matrix
    private Map crfsWithDefaultVersion;

    private String oid;

    public StudyEventDefinitionBean() {
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * @return Returns the category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category
     *            The category to set.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return Returns the crfs.
     */
    public ArrayList getCrfs() {
        return crfs;
    }

    /**
     * @param crfs
     *            The crfs to set.
     */
    public void setCrfs(ArrayList crfs) {
        this.crfs = crfs;
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
     * @return Returns the repeating.
     */
    public boolean isRepeating() {
        return repeating;
    }

    /**
     * @param repeating
     *            The repeating to set.
     */
    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    /**
     * @return Returns the studyId.
     */
    public int getStudyId() {
        return studyId;
    }

    /**
     * @param studyId
     *            The studyId to set.
     */
    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return Returns the ordinal.
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * @param ordinal
     *            The ordinal to set.
     */
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    /**
     * @return Returns the crfNum.
     */
    public int getCrfNum() {
        return crfNum;
    }

    /**
     * @param crfNum
     *            The crfNum to set.
     */
    public void setCrfNum(int crfNum) {
        this.crfNum = crfNum;
    }

    public Map getCrfsWithDefaultVersion() {
        return crfsWithDefaultVersion;
    }

    public void setCrfsWithDefaultVersion(Map crfsWithDefaultVersion) {
        this.crfsWithDefaultVersion = crfsWithDefaultVersion;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) obj;
        return sed.id == id;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return id;
    }
}