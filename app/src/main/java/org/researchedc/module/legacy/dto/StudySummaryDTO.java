package org.researchedc.module.legacy.dto;

import java.util.Date;

public class StudySummaryDTO {
    private int studyId;
    private String name;
    private String identifier;
    private String oid;
    private String type;
    private String status;
    private String principalInvestigator;
    private Date dateCreated;

    public int getStudyId() { return studyId; }
    public void setStudyId(int studyId) { this.studyId = studyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getOid() { return oid; }
    public void setOid(String oid) { this.oid = oid; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPrincipalInvestigator() { return principalInvestigator; }
    public void setPrincipalInvestigator(String pi) { this.principalInvestigator = pi; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
}
