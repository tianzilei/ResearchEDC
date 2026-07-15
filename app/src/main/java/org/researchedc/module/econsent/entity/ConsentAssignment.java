package org.researchedc.module.econsent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.researchedc.module.econsent.enums.ConsentAssignmentStatus;

@Entity(name = "ModuleConsentAssignment")
@Table(name = "consent_assignment")
public class ConsentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_id", nullable = false)
    private Integer studyId;

    @Column(name = "study_subject_id", nullable = false)
    private Integer studySubjectId;

    @Column(name = "consent_version_id", nullable = false)
    private Long consentVersionId;

    @Column(name = "participant_account_id", nullable = false)
    private Long participantAccountId;

    @Column(name = "participant_token_id")
    private Long participantTokenId;

    @Column(name = "task_instance_id")
    private Long taskInstanceId;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ConsentAssignmentStatus status = ConsentAssignmentStatus.ASSIGNED;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "entry_url", length = 500)
    private String entryUrl;

    @Column(name = "participant_name", length = 160)
    private String participantName;

    @Column(name = "participant_signature", length = 240)
    private String participantSignature;

    @Column(name = "participant_signed_at")
    private LocalDateTime participantSignedAt;

    @Column(name = "participant_evidence", length = 1000)
    private String participantEvidence;

    @Column(name = "countersigned_by")
    private Integer countersignedBy;

    @Column(name = "countersignature", length = 240)
    private String countersignature;

    @Column(name = "countersigned_at")
    private LocalDateTime countersignedAt;

    @Column(name = "artifact_name", length = 180)
    private String artifactName;

    @Lob
    @Column(name = "artifact_text")
    private String artifactText;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (status == null) {
            status = ConsentAssignmentStatus.ASSIGNED;
        }
    }

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

    public String getParticipantSignature() { return participantSignature; }
    public void setParticipantSignature(String participantSignature) { this.participantSignature = participantSignature; }

    public LocalDateTime getParticipantSignedAt() { return participantSignedAt; }
    public void setParticipantSignedAt(LocalDateTime participantSignedAt) { this.participantSignedAt = participantSignedAt; }

    public String getParticipantEvidence() { return participantEvidence; }
    public void setParticipantEvidence(String participantEvidence) { this.participantEvidence = participantEvidence; }

    public Integer getCountersignedBy() { return countersignedBy; }
    public void setCountersignedBy(Integer countersignedBy) { this.countersignedBy = countersignedBy; }

    public String getCountersignature() { return countersignature; }
    public void setCountersignature(String countersignature) { this.countersignature = countersignature; }

    public LocalDateTime getCountersignedAt() { return countersignedAt; }
    public void setCountersignedAt(LocalDateTime countersignedAt) { this.countersignedAt = countersignedAt; }

    public String getArtifactName() { return artifactName; }
    public void setArtifactName(String artifactName) { this.artifactName = artifactName; }

    public String getArtifactText() { return artifactText; }
    public void setArtifactText(String artifactText) { this.artifactText = artifactText; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
