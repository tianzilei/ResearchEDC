package org.researchedc.app.dto;

import org.researchedc.app.dto.AuditableEntity;

import java.util.Date;

public class StudySubjectDTO extends AuditableEntity {
    private Integer studySubjectId;
    private String label;
    private int subjectId;
    private int studyId;
    private java.time.LocalDateTime enrollmentDate;
    private String secondaryLabel;
    private String oid;
    private String ocOid;
    private String subjectUniqueIdentifier;
    private java.time.LocalDateTime dateCreated;
    private java.time.LocalDateTime dateUpdated;

    public StudySubjectDTO() {
        label = "";
        secondaryLabel = "";
    }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getSecondaryLabel() { return secondaryLabel; }
    public void setSecondaryLabel(String secondaryLabel) { this.secondaryLabel = secondaryLabel; }
    public int getStudyId() { return studyId; }
    public void setStudyId(int studyId) { this.studyId = studyId; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public java.time.LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(java.time.LocalDateTime enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public String getOid() { return oid; }
    public void setOid(String oid) { this.oid = oid; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String ocOid) { this.ocOid = ocOid; }
    public String getSubjectUniqueIdentifier() { return subjectUniqueIdentifier; }
    public void setSubjectUniqueIdentifier(String v) { this.subjectUniqueIdentifier = v; }
    public java.time.LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(java.time.LocalDateTime dateCreated) { this.dateCreated = dateCreated; }
    public java.time.LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(java.time.LocalDateTime dateUpdated) { this.dateUpdated = dateUpdated; }

    @Override
    public String getName() { return getLabel(); }

    @Override
    public void setName(String name) { setLabel(name); }
}
