/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.managestudy;

import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.core.Status;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author jxu
 *
 */
public class StudyEventBean extends AuditableEntityBean {
    public static final int STAGE_UNCOMPLETED = 1;
    public static final int STAGE_INITIAL_DATA_ENTRY_COMPLETE = 3;
    public static final int STAGE_DOUBLE_DATA_ENTRY_COMPLETE = 5;
    public static final int SUBJECT_EVENT_STATUS_SCHEDULED = 1;

    // STUDY_EVENT_ID STUDY_EVENT_DEFINITION_ID SUBJECT_ID
    // LOCATION SAMPLE_ORDINAL DATE_START DATE_END
    // OWNER_ID STATUS_ID DATE_CREATED DATE_UPDATED
    // UPDATE_ID
    private int studyEventDefinitionId;

    private int studySubjectId;

    private StudySubjectBean studySubject;

    private String location = "";

    private int sampleOrdinal;

    private Date dateStarted;

    private Date dateEnded;

    // not in database
    private StudyEventDefinitionBean studyEventDefinition =
      new StudyEventDefinitionBean();

    private ArrayList eventCRFs = new ArrayList();// not in DB

    private int stageId = STAGE_UNCOMPLETED;

    private int subjectEventStatusId = SUBJECT_EVENT_STATUS_SCHEDULED;

    private String studySubjectLabel;

    private boolean scheduledDatePast = false;// not in DB

    private int repeatingNum = 1;

    private ArrayList<StudyEventBean> repeatEvents = new ArrayList<StudyEventBean>();

    // A. Hamid.
    // will the edit icon be appeared
    private boolean editable = true;

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
     * @return the repeatEvents
     */
    public ArrayList<StudyEventBean> getRepeatEvents() {
        return repeatEvents;
    }

    /**
     * @param repeatEvents
     *            the repeatEvents to set
     */
    public void setRepeatEvents(ArrayList<StudyEventBean> repeatEvents) {
        this.repeatEvents = repeatEvents;
    }

    /**
     * @return Returns the repeatingNum.
     */
    public int getRepeatingNum() {
        return repeatingNum;
    }

    /**
     * @param repeatingNum
     *            The repeatingNum to set.
     */
    public void setRepeatingNum(int repeatingNum) {
        this.repeatingNum = repeatingNum;
    }

    /**
     * @return Returns the studySubjectLabel.
     */
    public String getStudySubjectLabel() {
        return studySubjectLabel;
    }

    /**
     * @param studySubjectLabel
     *            The studySubjectLabel to set.
     */
    public void setStudySubjectLabel(String studySubjectLabel) {
        this.studySubjectLabel = studySubjectLabel;
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

    public StudyEventBean() {
        stageId = STAGE_UNCOMPLETED;
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

    /**
     * @return Returns the studyEventDefinition.
     */
    public StudyEventDefinitionBean getStudyEventDefinition() {
        return studyEventDefinition;
    }

    /**
     * @param studyEventDefinition
     *            The studyEventDefinition to set.
     */
    public void setStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition) {
        this.studyEventDefinition = studyEventDefinition;
    }

    /**
     * @return Returns the eventCRFs.
     */
    public ArrayList getEventCRFs() {
        return eventCRFs;
    }

    /**
     * @param eventCRFs
     *            The eventCRFs to set.
     */
    public void setEventCRFs(ArrayList eventCRFs) {
        this.eventCRFs = eventCRFs;
    }

    /**
     * @return Returns the stage.
     */
    public String getStage() {
        return stageName(stageId);
    }

    public int getStageId() {
        return stageId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.bean.core.AuditableEntityBean#getStatus()
     */
    @Override
    public void setStatus(Status s) {
        this.status = s;

        if (s.equals(Status.AVAILABLE)) {
            stageId = STAGE_UNCOMPLETED;
        }

        else if (s.equals(Status.PENDING)) {
            stageId = STAGE_INITIAL_DATA_ENTRY_COMPLETE;
        }

        else if (s.equals(Status.UNAVAILABLE)) {
            stageId = STAGE_DOUBLE_DATA_ENTRY_COMPLETE;
        }
    }

    /**
     * @param stage
     *            The stage to set.
     */
    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public StudySubjectBean getStudySubject() {
        return studySubject;
    }

    public void setStudySubject(StudySubjectBean studySubject) {
        this.studySubject = studySubject;
    }

    private String stageName(int stageId) {
        return switch (stageId) {
            case STAGE_UNCOMPLETED -> "not_started";
            case STAGE_INITIAL_DATA_ENTRY_COMPLETE -> "initial_data_entry_complete";
            case STAGE_DOUBLE_DATA_ENTRY_COMPLETE -> "data_entry_complete";
            default -> "invalid";
        };
    }
}
