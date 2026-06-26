package org.researchedc.app.dto;

import org.researchedc.module.crf.dto.SectionDTO;

import org.researchedc.app.dto.AuditableEntity;

public class CrfVersionDTO extends AuditableEntity {
    private Integer crfVersionId;
    private String description;
    private int crfId;
    private int statusId;
    private String revisionNotes;
    private String oid;
    private String ocOid;
    private String xform;
    private String xformName;
    private String statusName;
    private java.util.List<SectionDTO> sections;

    public CrfVersionDTO() {
        description = "";
        revisionNotes = "";
    }

    public Integer getCrfVersionId() {
        return crfVersionId;
    }

    public void setCrfVersionId(Integer crfVersionId) {
        this.crfVersionId = crfVersionId;
    }

    public int getCrfId() {
        return crfId;
    }

    public void setCrfId(int crfId) {
        this.crfId = crfId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRevisionNotes() {
        return revisionNotes;
    }

    public void setRevisionNotes(String revisionNotes) {
        this.revisionNotes = revisionNotes;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOcOid() {
        return ocOid;
    }

    public void setOcOid(String ocOid) {
        this.ocOid = ocOid;
    }

    public String getXform() {
        return xform;
    }

    public void setXform(String xform) {
        this.xform = xform;
    }

    public String getXformName() {
        return xformName;
    }

    public void setXformName(String xformName) {
        this.xformName = xformName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public java.util.List<SectionDTO> getSections() {
        return sections;
    }

    public void setSections(java.util.List<SectionDTO> sections) {
        this.sections = sections;
    }
}
