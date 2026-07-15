package org.researchedc.module.econsent.dto;

public class ConsentArtifactDTO {
    private String artifactName;
    private String contentType;
    private String content;

    public String getArtifactName() { return artifactName; }
    public void setArtifactName(String artifactName) { this.artifactName = artifactName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
