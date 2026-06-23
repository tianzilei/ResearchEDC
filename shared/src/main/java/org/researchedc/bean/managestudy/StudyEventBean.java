/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.managestudy;

import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.core.Status;

import java.util.Date;

/**
 * @author jxu
 *
 */
public class StudyEventBean extends AuditableEntityBean {
    public static final int SUBJECT_EVENT_STATUS_SCHEDULED = 1;

    // STUDY_EVENT_ID STUDY_EVENT_DEFINITION_ID SUBJECT_ID
    // LOCATION SAMPLE_ORDINAL DATE_START DATE_END
    // OWNER_ID STATUS_ID DATE_CREATED DATE_UPDATED
    // UPDATE_ID
    private int studyEventDefinitionId;

    private int studySubjectId;

    private String location = "";

    private int sampleOrdinal;

    private Date dateStarted;

    private Date dateEnded;

    private int subjectEventStatusId = SUBJECT_EVENT_STATUS_SCHEDULED;

    // YW 08-17-2007
    private boolean startTimeFlag = false;
    private boolean endTimeFlag = false;

    /**
     * @return startTimeFlag
     */
    public boolean getStartTimeFlag() {
        return startTimeFlag;
    }

    /**
     *
     * @param startTimeFlag
     */
    public void setStartTimeFlag(boolean startTimeFlag) {
        this.startTimeFlag = startTimeFlag;
    }

    /**
     *
     * @return endTimeFlag
     */
    public boolean getEndTimeFlag() {
        return endTimeFlag;
    }

    /**
     *
     * @param endTimeFlag
     */
    public void setEndTimeFlag(boolean endTimeFlag) {
        this.endTimeFlag = endTimeFlag;
    }

    /**
     * @return Returns the subjectEventStatusId.
     */
    public int getSubjectEventStatusId() {
        return subjectEventStatusId;
    }

    /**
     * @param subjectEventStatusId
     *            The subjectEventStatusId to set.
     */
    public void setSubjectEventStatusId(int subjectEventStatusId) {
        this.subjectEventStatusId = subjectEventStatusId;
    }

    /**
     * @return Returns the dateEnded.
     */
    public Date getDateEnded() {
        return dateEnded;
    }

    /**
     * @param dateEnded
     *            The dateEnded to set.
     */
    public void setDateEnded(Date dateEnded) {
        this.dateEnded = dateEnded;
    }

    /**
     * @return Returns the dateStarted.
     */
    public Date getDateStarted() {
        return dateStarted;
    }

    /**
     * @param dateStarted
     *            The dateStarted to set.
     */
    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    /**
     * @return Returns the location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location
     *            The location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return Returns the sampleOrdinal.
     */
    public int getSampleOrdinal() {
        return sampleOrdinal;
    }

    /**
     * @param sampleOrdinal
     *            The sampleOrdinal to set.
     */
    public void setSampleOrdinal(int sampleOrdinal) {
        this.sampleOrdinal = sampleOrdinal;
    }

    /**
     * @return Returns the studyEventDefinitionId.
     */
    public int getStudyEventDefinitionId() {
        return studyEventDefinitionId;
    }

    /**
     * @param studyEventDefinitionId
     *            The studyEventDefinitionId to set.
     */
    public void setStudyEventDefinitionId(int studyEventDefinitionId) {
        this.studyEventDefinitionId = studyEventDefinitionId;
    }

    /**
     * @return Returns the studySubjectId.
     */
    public int getStudySubjectId() {
        return studySubjectId;
    }

    /**
     * @param studySubjectId
     *            The studySubjectId to set.
     */
    public void setStudySubjectId(int studySubjectId) {
        this.studySubjectId = studySubjectId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.bean.core.AuditableEntityBean#getStatus()
     */
    @Override
    public void setStatus(Status s) {
        this.status = s;
    }
}
