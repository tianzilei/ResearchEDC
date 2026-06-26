package org.researchedc.module.rule.dto;

public class RuleScheduleCheckRequest {
    private String serverZoneId;
    private String subjectZoneId;
    private Integer runTime;
    private Integer serverTime;

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

    public Integer getRunTime() {
        return runTime;
    }

    public void setRunTime(Integer runTime) {
        this.runTime = runTime;
    }

    public Integer getServerTime() {
        return serverTime;
    }

    public void setServerTime(Integer serverTime) {
        this.serverTime = serverTime;
    }
}
