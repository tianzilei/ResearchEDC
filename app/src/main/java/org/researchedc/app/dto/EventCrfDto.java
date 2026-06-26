package org.researchedc.app.dto;

import java.util.Date;

public class EventCrfDto extends AuditableEntity {
    private int studyEventId;
    private int crfVersionId;
    private Date dateInterviewed;
    private String interviewerName;
    private String annotations;
    private Date dateCompleted;
    private int validatorId;
    private Date dateValidate;
    private Date dateValidateCompleted;
    private String validatorAnnotations;
    private int studySubjectId;
    private boolean electronicSignatureStatus;
    private boolean sdvStatus;
    private int sdvUpdateId;

    public EventCrfDto() {
        interviewerName = "";
        annotations = "";
        validatorAnnotations = "";
    }

    public int getStudyEventId() { return studyEventId; }
    public void setStudyEventId(int v) { this.studyEventId = v; }
    public int getCRFVersionId() { return crfVersionId; }
    public void setCRFVersionId(int v) { this.crfVersionId = v; }
    public Date getDateInterviewed() { return dateInterviewed; }
    public void setDateInterviewed(Date v) { this.dateInterviewed = v; }
    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String v) { this.interviewerName = v; }
    public String getAnnotations() { return annotations; }
    public void setAnnotations(String v) { this.annotations = v; }
    public Date getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(Date v) { this.dateCompleted = v; }
    public int getValidatorId() { return validatorId; }
    public void setValidatorId(int v) { this.validatorId = v; }
    public Date getDateValidate() { return dateValidate; }
    public void setDateValidate(Date v) { this.dateValidate = v; }
    public Date getDateValidateCompleted() { return dateValidateCompleted; }
    public void setDateValidateCompleted(Date v) { this.dateValidateCompleted = v; }
    public String getValidatorAnnotations() { return validatorAnnotations; }
    public void setValidatorAnnotations(String v) { this.validatorAnnotations = v; }
    public int getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(int v) { this.studySubjectId = v; }
    public boolean isElectronicSignatureStatus() { return electronicSignatureStatus; }
    public void setElectronicSignatureStatus(boolean v) { this.electronicSignatureStatus = v; }
    public boolean isSdvStatus() { return sdvStatus; }
    public void setSdvStatus(boolean v) { this.sdvStatus = v; }
    public int getSdvUpdateId() { return sdvUpdateId; }
    public void setSdvUpdateId(int v) { this.sdvUpdateId = v; }
}
