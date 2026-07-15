package org.researchedc.module.participantaccess.dto;

public class IssuedParticipantTokenDTO {
    private ParticipantAccessTokenDTO token;
    private String rawToken;
    private String entryUrl;

    public ParticipantAccessTokenDTO getToken() { return token; }
    public void setToken(ParticipantAccessTokenDTO token) { this.token = token; }

    public String getRawToken() { return rawToken; }
    public void setRawToken(String rawToken) { this.rawToken = rawToken; }

    public String getEntryUrl() { return entryUrl; }
    public void setEntryUrl(String entryUrl) { this.entryUrl = entryUrl; }
}
