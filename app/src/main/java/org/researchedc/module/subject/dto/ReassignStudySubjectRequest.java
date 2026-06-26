package org.researchedc.module.subject.dto;

public class ReassignStudySubjectRequest {
    private Integer studyId;
    private String reason;

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }

    public String getReason() { return reason; }
    public void setReason(String v) { this.reason = v; }
}
