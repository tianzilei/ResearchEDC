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
    private int studyEventId = 0;
    private int CRFVersionId = 0;
    private Date dateInterviewed;
    private String interviewerName = "";
    // private int statusId =1;
    private Status status;
    private String annotations = "";
    private Date dateCompleted;
    private int validatorId = 0;
    private Date dateValidate;
    private Date dateValidateCompleted;
    private String validatorAnnotations = "";
    private int studySubjectId = 0;
    private boolean electronicSignatureStatus = false;
    private boolean sdvStatus = false;
    private int sdvUpdateId = 0;

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

    public int getSdvUpdateId() {
        return sdvUpdateId;
    }

    public void setSdvUpdateId(int sdvUpdateId) {
        this.sdvUpdateId = sdvUpdateId;
    }

}
