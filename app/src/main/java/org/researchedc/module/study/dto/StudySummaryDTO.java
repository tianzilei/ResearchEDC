package org.researchedc.module.study.dto;

import java.time.LocalDateTime;

public class StudySummaryDTO {
    private Integer studyId;
    private Integer parentStudyId;
    private boolean site;
    private String name;
    private String uniqueIdentifier;
    private String ocOid;
    private String phase;
    private String principalInvestigator;
    private String sponsor;
    private String status;
    private LocalDateTime dateCreated;
    private Integer expectedTotalEnrollment;

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getParentStudyId() { return parentStudyId; }
    public void setParentStudyId(Integer v) { this.parentStudyId = v; }
    public boolean isSite() { return site; }
    public void setSite(boolean v) { this.site = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getUniqueIdentifier() { return uniqueIdentifier; }
    public void setUniqueIdentifier(String v) { this.uniqueIdentifier = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public String getPhase() { return phase; }
    public void setPhase(String v) { this.phase = v; }
    public String getPrincipalInvestigator() { return principalInvestigator; }
    public void setPrincipalInvestigator(String v) { this.principalInvestigator = v; }
    public String getSponsor() { return sponsor; }
    public void setSponsor(String v) { this.sponsor = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public Integer getExpectedTotalEnrollment() { return expectedTotalEnrollment; }
    public void setExpectedTotalEnrollment(Integer v) { this.expectedTotalEnrollment = v; }
}
