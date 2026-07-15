package org.researchedc.module.econsent.dto;

public class CreateConsentVersionRequest {
    private String versionLabel;
    private String bodyText;

    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }

    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }
}
