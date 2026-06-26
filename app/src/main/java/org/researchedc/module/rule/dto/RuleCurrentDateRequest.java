package org.researchedc.module.rule.dto;

public class RuleCurrentDateRequest {
    private String serverZoneId;
    private String subjectZoneId;

    public String getServerZoneId() {
        return serverZoneId;
    }

    public void setServerZoneId(String serverZoneId) {
        this.serverZoneId = serverZoneId;
    }

    public String getSubjectZoneId() {
        return subjectZoneId;
    }

    public void setSubjectZoneId(String subjectZoneId) {
        this.subjectZoneId = subjectZoneId;
    }
}
