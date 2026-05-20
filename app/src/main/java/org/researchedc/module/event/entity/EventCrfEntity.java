package org.researchedc.module.event.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleEventCrf")
@Table(name = "event_crf")
public class EventCrfEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ecrf_seq")
    @SequenceGenerator(name = "ecrf_seq", sequenceName = "event_crf_event_crf_id_seq", allocationSize = 1)
    @Column(name = "event_crf_id")
    private Integer eventCrfId;

    @Column(name = "study_event_id")
    private Integer studyEventId;

    @Column(name = "study_subject_id")
    private Integer studySubjectId;

    @Column(name = "crf_version_id")
    private Integer crfVersionId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "date_interviewed")
    private LocalDateTime dateInterviewed;

    @Column(name = "interviewer_name", length = 255)
    private String interviewerName;

    @Column(length = 4000)
    private String annotations;

    @Column(name = "date_completed")
    private LocalDateTime dateCompleted;

    @Column(name = "validator_id")
    private Integer validatorId;

    @Column(name = "date_validate")
    private LocalDateTime dateValidate;

    @Column(name = "date_validate_completed")
    private LocalDateTime dateValidateCompleted;

    @Column(name = "validator_annotations", length = 4000)
    private String validatorAnnotations;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "electronic_signature_status")
    private Boolean electronicSignatureStatus;

    @Column(name = "sdv_status")
    private Boolean sdvStatus;

    @Column(name = "old_status_id")
    private Integer oldStatusId;

    @Column(name = "sdv_update_id")
    private Integer sdvUpdateId;

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
    public String getAnnotations() { return annotations; }
    public void setAnnotations(String v) { this.annotations = v; }
    public LocalDateTime getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(LocalDateTime v) { this.dateCompleted = v; }
    public Integer getValidatorId() { return validatorId; }
    public void setValidatorId(Integer v) { this.validatorId = v; }
    public LocalDateTime getDateValidate() { return dateValidate; }
    public void setDateValidate(LocalDateTime v) { this.dateValidate = v; }
    public LocalDateTime getDateValidateCompleted() { return dateValidateCompleted; }
    public void setDateValidateCompleted(LocalDateTime v) { this.dateValidateCompleted = v; }
    public String getValidatorAnnotations() { return validatorAnnotations; }
    public void setValidatorAnnotations(String v) { this.validatorAnnotations = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
    public Boolean getElectronicSignatureStatus() { return electronicSignatureStatus; }
    public void setElectronicSignatureStatus(Boolean v) { this.electronicSignatureStatus = v; }
    public Boolean getSdvStatus() { return sdvStatus; }
    public void setSdvStatus(Boolean v) { this.sdvStatus = v; }
    public Integer getOldStatusId() { return oldStatusId; }
    public void setOldStatusId(Integer v) { this.oldStatusId = v; }
    public Integer getSdvUpdateId() { return sdvUpdateId; }
    public void setSdvUpdateId(Integer v) { this.sdvUpdateId = v; }
}
