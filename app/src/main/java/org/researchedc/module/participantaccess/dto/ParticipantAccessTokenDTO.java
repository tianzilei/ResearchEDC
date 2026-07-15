package org.researchedc.module.participantaccess.dto;

import java.time.LocalDateTime;
import org.researchedc.module.participantaccess.enums.ParticipantAccessTokenStatus;

public class ParticipantAccessTokenDTO {
    private Long id;
    private Long participantAccountId;
    private Integer studyId;
    private Integer studySubjectId;
    private String scope;
    private ParticipantAccessTokenStatus status;
    private Integer issuedBy;
    private LocalDateTime issuedDate;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private Integer revokedBy;
    private LocalDateTime revokedDate;
    private String revocationReason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParticipantAccountId() { return participantAccountId; }
    public void setParticipantAccountId(Long participantAccountId) { this.participantAccountId = participantAccountId; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

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
