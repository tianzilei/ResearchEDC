package org.researchedc.module.event.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleEventDefinitionCrf")
@Table(name = "event_definition_crf")
public class EventDefinitionCrfEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "edcrf_seq")
    @SequenceGenerator(name = "edcrf_seq", sequenceName = "event_definition_crf_id_seq", allocationSize = 1)
    @Column(name = "event_definition_crf_id")
    private Integer eventDefinitionCrfId;

    @Column(name = "study_event_definition_id")
    private Integer studyEventDefinitionId;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(name = "crf_id")
    private Integer crfId;

    @Column(name = "required_crf")
    private Boolean requiredCrf;

    @Column(name = "double_entry")
    private Boolean doubleEntry;

    @Column(name = "require_all_text_filled")
    private Boolean requireAllTextFilled;

    @Column(name = "decision_conditions")
    private Boolean decisionConditions;

    @Column(name = "null_values", length = 255)
    private String nullValues;

    @Column(name = "default_version_id")
    private Integer defaultVersionId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "ordinal")
    private Integer ordinal;

    @Column(name = "electronic_signature")
    private Boolean electronicSignature;

    @Column(name = "hide_crf")
    private Boolean hideCrf;

    @Column(name = "source_data_verification_code")
    private Integer sourceDataVerificationCode;

    @Column(name = "selected_version_ids", length = 150)
    private String selectedVersionIds;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "participant_form")
    private Boolean participantForm;

    @Column(name = "allow_anonymous_submission")
    private Boolean allowAnonymousSubmission;

    @Column(name = "submission_url", length = 255)
    private String submissionUrl;

    public Integer getEventDefinitionCrfId() { return eventDefinitionCrfId; }
    public void setEventDefinitionCrfId(Integer v) { this.eventDefinitionCrfId = v; }
    public Integer getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public void setStudyEventDefinitionId(Integer v) { this.studyEventDefinitionId = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getCrfId() { return crfId; }
    public void setCrfId(Integer v) { this.crfId = v; }
    public Boolean getRequiredCrf() { return requiredCrf; }
    public void setRequiredCrf(Boolean v) { this.requiredCrf = v; }
    public Boolean getDoubleEntry() { return doubleEntry; }
    public void setDoubleEntry(Boolean v) { this.doubleEntry = v; }
    public Boolean getRequireAllTextFilled() { return requireAllTextFilled; }
    public void setRequireAllTextFilled(Boolean v) { this.requireAllTextFilled = v; }
    public Boolean getDecisionConditions() { return decisionConditions; }
    public void setDecisionConditions(Boolean v) { this.decisionConditions = v; }
    public String getNullValues() { return nullValues; }
    public void setNullValues(String v) { this.nullValues = v; }
    public Integer getDefaultVersionId() { return defaultVersionId; }
    public void setDefaultVersionId(Integer v) { this.defaultVersionId = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer v) { this.ordinal = v; }
    public Boolean getElectronicSignature() { return electronicSignature; }
    public void setElectronicSignature(Boolean v) { this.electronicSignature = v; }
    public Boolean getHideCrf() { return hideCrf; }
    public void setHideCrf(Boolean v) { this.hideCrf = v; }
    public Integer getSourceDataVerificationCode() { return sourceDataVerificationCode; }
    public void setSourceDataVerificationCode(Integer v) { this.sourceDataVerificationCode = v; }
    public String getSelectedVersionIds() { return selectedVersionIds; }
    public void setSelectedVersionIds(String v) { this.selectedVersionIds = v; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer v) { this.parentId = v; }
    public Boolean getParticipantForm() { return participantForm; }
    public void setParticipantForm(Boolean v) { this.participantForm = v; }
    public Boolean getAllowAnonymousSubmission() { return allowAnonymousSubmission; }
    public void setAllowAnonymousSubmission(Boolean v) { this.allowAnonymousSubmission = v; }
    public String getSubmissionUrl() { return submissionUrl; }
    public void setSubmissionUrl(String v) { this.submissionUrl = v; }
}
