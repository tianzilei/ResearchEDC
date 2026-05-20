package org.researchedc.module.subject.application.command;

import java.time.LocalDateTime;

public class CreateSubjectCommand {

    private String uniqueIdentifier;
    private LocalDateTime dateOfBirth;
    private String gender;
    private Boolean dobCollected;

    public CreateSubjectCommand() {
    }

    public CreateSubjectCommand(String uniqueIdentifier, LocalDateTime dateOfBirth,
                                String gender, Boolean dobCollected) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.dobCollected = dobCollected;
    }

    public String getUniqueIdentifier() { return uniqueIdentifier; }
    public void setUniqueIdentifier(String uniqueIdentifier) { this.uniqueIdentifier = uniqueIdentifier; }

    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Boolean getDobCollected() { return dobCollected; }
    public void setDobCollected(Boolean dobCollected) { this.dobCollected = dobCollected; }
}
