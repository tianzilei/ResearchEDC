/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.managestudy;

import org.researchedc.bean.core.AuditableEntityBean;

import java.util.Date;

/**
 * @author jxu
 *
 */
public class StudySubjectBean extends AuditableEntityBean {
    // STUDY_SUBJECT_ID, LABEL, SUBJECT_ID, STUDY_ID
    // STATUS_ID, DATE_CREATED, OWNER_ID,
    // DATE_UPDATED, UPDATE_ID,secondary_label
    private String label = "";

    private int subjectId;

    private int studyId;
    
    private Date enrollmentDate;

    private String secondaryLabel = "";

    /**
     * The OID, used for export and import of data.
     */
    private String oid;
    	
	public StudySubjectBean() {
    }

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Returns the secondaryLabel.
     */
    public String getSecondaryLabel() {
        return secondaryLabel;
    }

    /**
     * @param secondaryLabel
     *            The secondaryLabel to set.
     */
    public void setSecondaryLabel(String secondaryLabel) {
        this.secondaryLabel = secondaryLabel;
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
     * @return Returns the subjectId.
     */
    public int getSubjectId() {
        return subjectId;
    }

    /**
     * @param subjectId
     *            The subjectId to set.
     */
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    /**
     * @return Returns the enrollmentDate.
     */
    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    /**
     * @param enrollmentDate
     *            The enrollmentDate to set.
     */
    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    // disambiguate the meaning of "name" in this context
    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public void setName(String name) {
        setLabel(name);
    }

}
