package org.researchedc.module.event.dto;

import java.time.LocalDateTime;

public class EventCrfDTO {
    private Integer eventCrfId;
    private Integer studyEventId;
    private Integer studySubjectId;
    private Integer crfVersionId;
    private Integer statusId;
    private LocalDateTime dateInterviewed;
    private String interviewerName;
    private LocalDateTime dateCompleted;
    private LocalDateTime dateValidate;
    private Boolean electronicSignatureStatus;
    private Boolean sdvStatus;
    private LocalDateTime dateCreated;

    public Integer getEventCrfId() { return eventCrfId; }
    public void setEventCrfId(Integer v) { this.eventCrfId = v; }
    public Integer getStudyEventId() { return studyEventId; }
    public void setStudyEventId(Integer v) { this.studyEventId = v; }
    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer v) { this.studySubjectId = v; }
    public Integer getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(Integer v) { this.crfVersionId = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public LocalDateTime getDateInterviewed() { return dateInterviewed; }
    public void setDateInterviewed(LocalDateTime v) { this.dateInterviewed = v; }
    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String v) { this.interviewerName = v; }
    public LocalDateTime getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(LocalDateTime v) { this.dateCompleted = v; }
    public LocalDateTime getDateValidate() { return dateValidate; }
    public void setDateValidate(LocalDateTime v) { this.dateValidate = v; }
    public Boolean getElectronicSignatureStatus() { return electronicSignatureStatus; }
    public void setElectronicSignatureStatus(Boolean v) { this.electronicSignatureStatus = v; }
    public Boolean getSdvStatus() { return sdvStatus; }
    public void setSdvStatus(Boolean v) { this.sdvStatus = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
}
