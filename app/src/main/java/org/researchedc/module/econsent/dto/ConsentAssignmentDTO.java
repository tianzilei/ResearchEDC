package org.researchedc.module.econsent.dto;

import java.time.LocalDateTime;
import org.researchedc.module.econsent.enums.ConsentAssignmentStatus;

public class ConsentAssignmentDTO {
    private Long id;
    private Integer studyId;
    private Integer studySubjectId;
    private Long consentVersionId;
    private Long participantAccountId;
    private Long participantTokenId;
    private Long taskInstanceId;
    private ConsentAssignmentStatus status;
    private LocalDateTime dueAt;
    private String entryUrl;
    private String participantName;
    private LocalDateTime participantSignedAt;
    private Integer countersignedBy;
    private LocalDateTime countersignedAt;
    private String artifactName;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

    public Long getConsentVersionId() { return consentVersionId; }
    public void setConsentVersionId(Long consentVersionId) { this.consentVersionId = consentVersionId; }

    public Long getParticipantAccountId() { return participantAccountId; }
    public void setParticipantAccountId(Long participantAccountId) { this.participantAccountId = participantAccountId; }

    public Long getParticipantTokenId() { return participantTokenId; }
    public void setParticipantTokenId(Long participantTokenId) { this.participantTokenId = participantTokenId; }

    public Long getTaskInstanceId() { return taskInstanceId; }
    public void setTaskInstanceId(Long taskInstanceId) { this.taskInstanceId = taskInstanceId; }

    public ConsentAssignmentStatus getStatus() { return status; }
    public void setStatus(ConsentAssignmentStatus status) { this.status = status; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    public String getEntryUrl() { return entryUrl; }
    public void setEntryUrl(String entryUrl) { this.entryUrl = entryUrl; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public LocalDateTime getParticipantSignedAt() { return participantSignedAt; }
    public void setParticipantSignedAt(LocalDateTime participantSignedAt) { this.participantSignedAt = participantSignedAt; }

    public Integer getCountersignedBy() { return countersignedBy; }
    public void setCountersignedBy(Integer countersignedBy) { this.countersignedBy = countersignedBy; }

    public LocalDateTime getCountersignedAt() { return countersignedAt; }
    public void setCountersignedAt(LocalDateTime countersignedAt) { this.countersignedAt = countersignedAt; }

    public String getArtifactName() { return artifactName; }
    public void setArtifactName(String artifactName) { this.artifactName = artifactName; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
