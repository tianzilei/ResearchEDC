package org.researchedc.module.econsent.dto;

public class ConsentAssignmentResultDTO {
    private ConsentAssignmentDTO assignment;
    private String participantEntryUrl;

    public ConsentAssignmentDTO getAssignment() { return assignment; }
    public void setAssignment(ConsentAssignmentDTO assignment) { this.assignment = assignment; }

    public String getParticipantEntryUrl() { return participantEntryUrl; }
    public void setParticipantEntryUrl(String participantEntryUrl) { this.participantEntryUrl = participantEntryUrl; }
}
