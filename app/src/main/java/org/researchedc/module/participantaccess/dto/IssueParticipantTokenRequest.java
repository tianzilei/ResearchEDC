package org.researchedc.module.participantaccess.dto;

public class IssueParticipantTokenRequest {
    private Long participantAccountId;
    private Integer expiresInHours;
    private String scope;

    public Long getParticipantAccountId() { return participantAccountId; }
    public void setParticipantAccountId(Long participantAccountId) { this.participantAccountId = participantAccountId; }

    public Integer getExpiresInHours() { return expiresInHours; }
    public void setExpiresInHours(Integer expiresInHours) { this.expiresInHours = expiresInHours; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
}
