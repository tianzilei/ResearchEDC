package org.akaza.openclinica.module.crf.dto;

import java.util.Date;
import java.util.List;

public class CrfVersionDTO {
    private int crfVersionId;
    private int crfId;
    private String name;
    private String description;
    private String revisionNotes;
    private String ocOid;
    private String status;
    private Date dateCreated;
    private List<SectionDTO> sections;

    public int getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(int crfVersionId) { this.crfVersionId = crfVersionId; }
    public int getCrfId() { return crfId; }
    public void setCrfId(int crfId) { this.crfId = crfId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRevisionNotes() { return revisionNotes; }
    public void setRevisionNotes(String revisionNotes) { this.revisionNotes = revisionNotes; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String ocOid) { this.ocOid = ocOid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
    public List<SectionDTO> getSections() { return sections; }
    public void setSections(List<SectionDTO> sections) { this.sections = sections; }
}
