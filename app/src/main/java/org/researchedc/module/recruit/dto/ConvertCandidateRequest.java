package org.researchedc.module.recruit.dto;

import java.time.LocalDateTime;

public class ConvertCandidateRequest {
    private String subjectUniqueIdentifier;
    private String studySubjectLabel;
    private String gender;
    private LocalDateTime dateOfBirth;
    private LocalDateTime enrollmentDate;

    public String getSubjectUniqueIdentifier() { return subjectUniqueIdentifier; }
    public void setSubjectUniqueIdentifier(String subjectUniqueIdentifier) {
        this.subjectUniqueIdentifier = subjectUniqueIdentifier;
    }

    public String getStudySubjectLabel() { return studySubjectLabel; }
    public void setStudySubjectLabel(String studySubjectLabel) { this.studySubjectLabel = studySubjectLabel; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime enrollmentDate) { this.enrollmentDate = enrollmentDate; }
}
