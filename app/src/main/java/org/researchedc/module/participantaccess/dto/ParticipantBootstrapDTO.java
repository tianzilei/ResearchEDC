package org.researchedc.module.participantaccess.dto;

import java.time.LocalDateTime;

public class ParticipantBootstrapDTO {
    private Long tokenId;
    private Long participantAccountId;
    private Integer studyId;
    private Integer studySubjectId;
    private String displayLabel;
    private String preferredLocale;
    private String scope;
    private LocalDateTime expiresAt;

    public Long getTokenId() { return tokenId; }
    public void setTokenId(Long tokenId) { this.tokenId = tokenId; }

    public Long getParticipantAccountId() { return participantAccountId; }
    public void setParticipantAccountId(Long participantAccountId) { this.participantAccountId = participantAccountId; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

    public String getDisplayLabel() { return displayLabel; }
    public void setDisplayLabel(String displayLabel) { this.displayLabel = displayLabel; }

    public String getPreferredLocale() { return preferredLocale; }
    public void setPreferredLocale(String preferredLocale) { this.preferredLocale = preferredLocale; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
