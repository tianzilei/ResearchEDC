package org.akaza.openclinica.module.subject.dto;

import java.time.LocalDateTime;

public class StudySubjectDTO {
    private Integer studySubjectId;
    private Integer studyId;
    private Integer subjectId;
    private String label;
    private String secondaryLabel;
    private String ocOid;
    private LocalDateTime enrollmentDate;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String subjectUniqueIdentifier;

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer v) { this.studySubjectId = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer v) { this.subjectId = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public String getSecondaryLabel() { return secondaryLabel; }
    public void setSecondaryLabel(String v) { this.secondaryLabel = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime v) { this.enrollmentDate = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public String getSubjectUniqueIdentifier() { return subjectUniqueIdentifier; }
    public void setSubjectUniqueIdentifier(String v) { this.subjectUniqueIdentifier = v; }
}
