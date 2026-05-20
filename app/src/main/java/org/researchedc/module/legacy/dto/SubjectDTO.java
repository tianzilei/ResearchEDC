package org.researchedc.module.legacy.dto;

import java.util.Date;

public class SubjectDTO {
    private int studySubjectId;
    private int studyId;
    private String label;
    private String secondaryLabel;
    private String uniqueIdentifier;
    private Date enrollmentDate;
    private String status;

    public int getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(int v) { this.studySubjectId = v; }
    public int getStudyId() { return studyId; }
    public void setStudyId(int v) { this.studyId = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public String getSecondaryLabel() { return secondaryLabel; }
    public void setSecondaryLabel(String v) { this.secondaryLabel = v; }
    public String getUniqueIdentifier() { return uniqueIdentifier; }
    public void setUniqueIdentifier(String v) { this.uniqueIdentifier = v; }
    public Date getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(Date v) { this.enrollmentDate = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
