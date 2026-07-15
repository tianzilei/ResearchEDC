package org.researchedc.module.participantaccess.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.researchedc.module.participantaccess.enums.ParticipantAccessTokenStatus;

@Entity(name = "ModuleParticipantAccessToken")
@Table(name = "participant_access_token")
public class ParticipantAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "participant_account_id", nullable = false)
    private Long participantAccountId;

    @Column(name = "study_id", nullable = false)
    private Integer studyId;

    @Column(name = "study_subject_id", nullable = false)
    private Integer studySubjectId;

    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @Column(nullable = false, length = 60)
    private String scope;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ParticipantAccessTokenStatus status = ParticipantAccessTokenStatus.ACTIVE;

    @Column(name = "issued_by")
    private Integer issuedBy;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked_by")
    private Integer revokedBy;

    @Column(name = "revoked_date")
    private LocalDateTime revokedDate;

    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

    @PrePersist
    void onCreate() {
        if (issuedDate == null) {
            issuedDate = LocalDateTime.now();
        }
        if (status == null) {
            status = ParticipantAccessTokenStatus.ACTIVE;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParticipantAccountId() { return participantAccountId; }
    public void setParticipantAccountId(Long participantAccountId) { this.participantAccountId = participantAccountId; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public ParticipantAccessTokenStatus getStatus() { return status; }
    public void setStatus(ParticipantAccessTokenStatus status) { this.status = status; }

    public Integer getIssuedBy() { return issuedBy; }
    public void setIssuedBy(Integer issuedBy) { this.issuedBy = issuedBy; }

    public LocalDateTime getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDateTime issuedDate) { this.issuedDate = issuedDate; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public Integer getRevokedBy() { return revokedBy; }
    public void setRevokedBy(Integer revokedBy) { this.revokedBy = revokedBy; }

    public LocalDateTime getRevokedDate() { return revokedDate; }
    public void setRevokedDate(LocalDateTime revokedDate) { this.revokedDate = revokedDate; }

    public String getRevocationReason() { return revocationReason; }
    public void setRevocationReason(String revocationReason) { this.revocationReason = revocationReason; }
}
