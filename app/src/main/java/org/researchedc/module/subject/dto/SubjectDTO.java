package org.researchedc.module.subject.dto;

import java.time.LocalDateTime;

public class SubjectDTO {
    private Integer subjectId;
    private String uniqueIdentifier;
    private LocalDateTime dateOfBirth;
    private String gender;
    private Boolean dobCollected;
    private LocalDateTime dateCreated;

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer v) { this.subjectId = v; }
    public String getUniqueIdentifier() { return uniqueIdentifier; }
    public void setUniqueIdentifier(String v) { this.uniqueIdentifier = v; }
    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime v) { this.dateOfBirth = v; }
    public String getGender() { return gender; }
    public void setGender(String v) { this.gender = v; }
    public Boolean getDobCollected() { return dobCollected; }
    public void setDobCollected(Boolean v) { this.dobCollected = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
}
