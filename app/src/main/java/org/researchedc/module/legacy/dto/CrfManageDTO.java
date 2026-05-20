package org.researchedc.module.legacy.dto;

import java.util.Date;
import java.util.List;

public class CrfManageDTO {
    private int crfId;
    private String name;
    private String description;
    private String ocOid;
    private String status;
    private Date dateCreated;
    private List<CrfVersionManageDTO> versions;

    public int getCrfId() { return crfId; }
    public void setCrfId(int v) { this.crfId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date v) { this.dateCreated = v; }
    public List<CrfVersionManageDTO> getVersions() { return versions; }
    public void setVersions(List<CrfVersionManageDTO> v) { this.versions = v; }
}
