/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.submit;

import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.core.Status;

import java.util.Date;

/**
 * <P>
 * EventCRFBean, the object that collects data on a subject while filling out a
 * CRF. Previous equivalent was individualInstrumentBean in version v.1.
 *
 * @author thickerson
 */
public class EventCRFBean extends AuditableEntityBean {
    public static final int STAGE_INVALID = 0;
    public static final int STAGE_UNCOMPLETED = 1;
    public static final int STAGE_INITIAL_DATA_ENTRY = 2;
    public static final int STAGE_INITIAL_DATA_ENTRY_COMPLETE = 3;
    public static final int STAGE_DOUBLE_DATA_ENTRY = 4;
    public static final int STAGE_DOUBLE_DATA_ENTRY_COMPLETE = 5;
    public static final int STAGE_LOCKED = 7;

    private int studyEventId = 0;
    private int CRFVersionId = 0;
    private Date dateInterviewed;
    private String interviewerName = "";
    private int completionStatusId = 0;
    // private int statusId =1;
    private Status status;
    private String annotations = "";
    private Date dateCompleted;
    private int validatorId = 0;
    private Date dateValidate;
    private Date dateValidateCompleted;
    private String validatorAnnotations = "";
    private String validateString = "";
    private int studySubjectId = 0;
    private boolean electronicSignatureStatus = false;
    private boolean sdvStatus = false;
    private int sdvUpdateId = 0;
    //the following are not in the table
    private String studySubjectName = "";
    private String eventName = "";
    private String studyName = "";
    private int eventOrdinal = 1;

    private int stageId = STAGE_INVALID;

    public EventCRFBean() {
        status = Status.INVALID;
    }

    public boolean isSdvStatus() {
        return sdvStatus;
    }

    public void setSdvStatus(boolean sdvStatus) {
        this.sdvStatus = sdvStatus;
    }

    /**
     * @return Returns the annotations.
     */
    public String getAnnotations() {
        return annotations;
    }

    /**
     * @param annotations
     *            The annotations to set.
     */
    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    /**
     * @return Returns the completionStatusId.
     */
    public int getCompletionStatusId() {
        return completionStatusId;
    }

    /**
     * @param completionStatusId
     *            The completionStatusId to set.
     */
    public void setCompletionStatusId(int completionStatusId) {
        this.completionStatusId = completionStatusId;
    }

    /**
     * @return Returns the cRFVersionId.
     */
    public int getCRFVersionId() {
        return CRFVersionId;
    }

    /**
     * @param versionId
     *            The cRFVersionId to set.
     */
    public void setCRFVersionId(int versionId) {
        CRFVersionId = versionId;
    }

    /**
     * @return Returns the dateCompleted.
     */
    public Date getDateCompleted() {
        return dateCompleted;
    }

    /**
     * @param dateCompleted
     *            The dateCompleted to set.
     */
    public void setDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    /**
     * @return Returns the dateInterviewed.
     */
    public Date getDateInterviewed() {
        return dateInterviewed;
    }

    /**
     * @param dateInterviewed
     *            The dateInterviewed to set.
     */
    public void setDateInterviewed(Date dateInterviewed) {
        this.dateInterviewed = dateInterviewed;
    }

    /**
     * @return Returns the dateValidate.
     */
    public Date getDateValidate() {
        return dateValidate;
    }

    /**
     * @param dateValidate
     *            The dateValidate to set.
     */
    public void setDateValidate(Date dateValidate) {
        this.dateValidate = dateValidate;
    }

    /**
     * @return Returns the dateValidateCompleted.
     */
    public Date getDateValidateCompleted() {
        return dateValidateCompleted;
    }

    /**
     * @param dateValidateCompleted
     *            The dateValidateCompleted to set.
     */
    public void setDateValidateCompleted(Date dateValidateCompleted) {
        this.dateValidateCompleted = dateValidateCompleted;
    }

    /**
     * @return Returns the interviewerName.
     */
    public String getInterviewerName() {
        return interviewerName;
    }

    /**
     * @param interviewerName
     *            The interviewerName to set.
     */
    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    /**
     * @return Returns the status.
     */
    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * @param status
     *            The status to set.
     */
    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return Returns the studyEventId.
     */
    public int getStudyEventId() {
        return studyEventId;
    }

    /**
     * @param studyEventId
     *            The studyEventId to set.
     */
    public void setStudyEventId(int studyEventId) {
        this.studyEventId = studyEventId;
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
     * @return Returns the validateString.
     */
    public String getValidateString() {
        return validateString;
    }

    /**
     * @param validateString
     *            The validateString to set.
     */
    public void setValidateString(String validateString) {
        this.validateString = validateString;
    }

    /**
     * @return Returns the validatorAnnotations.
     */
    public String getValidatorAnnotations() {
        return validatorAnnotations;
    }

    /**
     * @param validatorAnnotations
     *            The validatorAnnotations to set.
     */
    public void setValidatorAnnotations(String validatorAnnotations) {
        this.validatorAnnotations = validatorAnnotations;
    }

    /**
     * @return Returns the validatorId.
     */
    public int getValidatorId() {
        return validatorId;
    }

    /**
     * @param validatorId
     *            The validatorId to set.
     */
    public void setValidatorId(int validatorId) {
        this.validatorId = validatorId;
    }

    /**
     * Uses the status and created/updated dates to determine which stage the
     * Event CRF is in.
     *
     * @return The Event CRF's data entry stage.
     */
    public String getStage() {
        return stageName(getStageId());
    }

    public int getStageId() {
        if (stageId != STAGE_INVALID) {
            return stageId;
        }

        if (!active || !status.isActive()) {
            stageId = STAGE_UNCOMPLETED;
        }

        if (status.equals(Status.AVAILABLE)) {
            stageId = STAGE_INITIAL_DATA_ENTRY;
        }

        if (status.equals(Status.PENDING)) {
            if (validatorId != 0) {
                stageId = STAGE_DOUBLE_DATA_ENTRY;
            } else {
                stageId = STAGE_INITIAL_DATA_ENTRY_COMPLETE;
            }
        }

        if (status.equals(Status.UNAVAILABLE)) {
            stageId = STAGE_DOUBLE_DATA_ENTRY_COMPLETE;
        }

        if (status.equals(Status.LOCKED)) {
            stageId = STAGE_LOCKED;
        }

        return stageId;
    }

    /**
     * The problem with the above data entry stage getter is that you can never
     * set the stage back to 'invalid'.
     *
     *            invalidate, allowing us to invalidate the stage again
     */
    // public String getStage(boolean invalidate) {
    // if (stageId != STAGE_INVALID) {
    // if (invalidate) {
    // stageId = STAGE_INVALID;
    // return stageName(stageId);
    // } else {
    // return stageName(stageId);
    // }
    // }
    // return getStage();
    // }
    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    /**
     * @return Returns the electronicSignatureStatus.
     */
    public boolean isElectronicSignatureStatus() {
        return electronicSignatureStatus;
    }

    /**
     * @param electronicSignatureStatus
     *            The electronicSignatureStatus to set.
     */
    public void setElectronicSignatureStatus(boolean electronicSignatureStatus) {
        this.electronicSignatureStatus = electronicSignatureStatus;
    }

    /**
     * @return the eventName
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * @param eventName the eventName to set
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * @return the studyName
     */
    public String getStudyName() {
        return studyName;
    }

    /**
     * @param studyName the studyName to set
     */
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    /**
     * @return the studySubjectName
     */
    public String getStudySubjectName() {
        return studySubjectName;
    }

    /**
     * @param studySubjectName the studySubjectName to set
     */
    public void setStudySubjectName(String studySubjectName) {
        this.studySubjectName = studySubjectName;
    }

    public void setEventOrdinal(int i) {
        this.eventOrdinal = i;
    }

    public int getEventOrdinal() {
        return this.eventOrdinal;
    }

    public int getSdvUpdateId() {
        return sdvUpdateId;
    }

    public void setSdvUpdateId(int sdvUpdateId) {
        this.sdvUpdateId = sdvUpdateId;
    }

    private String stageName(int stageId) {
        return switch (stageId) {
            case STAGE_UNCOMPLETED -> "not_started";
            case STAGE_INITIAL_DATA_ENTRY -> "initial_data_entry";
            case STAGE_INITIAL_DATA_ENTRY_COMPLETE -> "initial_data_entry_complete";
            case STAGE_DOUBLE_DATA_ENTRY -> "double_data_entry";
            case STAGE_DOUBLE_DATA_ENTRY_COMPLETE -> "data_entry_complete";
            case STAGE_LOCKED -> "locked";
            default -> "invalid";
        };
    }

}
