package org.researchedc.module.subject.application.command;

import java.time.LocalDateTime;

public class EnrollSubjectCommand {

    private Integer studyId;
    private Integer subjectId;
    private String label;
    private String secondaryLabel;
    private LocalDateTime enrollmentDate;
    private String ocOid;

    public EnrollSubjectCommand() {
    }

    public EnrollSubjectCommand(Integer studyId, Integer subjectId, String label,
                                String secondaryLabel, LocalDateTime enrollmentDate, String ocOid) {
        this.studyId = studyId;
        this.subjectId = subjectId;
        this.label = label;
        this.secondaryLabel = secondaryLabel;
        this.enrollmentDate = enrollmentDate;
        this.ocOid = ocOid;
    }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getSecondaryLabel() { return secondaryLabel; }
    public void setSecondaryLabel(String secondaryLabel) { this.secondaryLabel = secondaryLabel; }

    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getOcOid() { return ocOid; }
    public void setOcOid(String ocOid) { this.ocOid = ocOid; }
}
