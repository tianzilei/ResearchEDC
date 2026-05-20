package org.researchedc.module.crf.dto;

import java.util.Date;
import java.util.List;

public class CrfSummaryDTO {
    private int crfId;
    private String name;
    private String description;
    private String ocOid;
    private String status;
    private int versionCount;
    private Date dateCreated;
    private Date dateUpdated;

    public int getCrfId() { return crfId; }
    public void setCrfId(int crfId) { this.crfId = crfId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String ocOid) { this.ocOid = ocOid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getVersionCount() { return versionCount; }
    public void setVersionCount(int versionCount) { this.versionCount = versionCount; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
    public Date getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(Date dateUpdated) { this.dateUpdated = dateUpdated; }
}
