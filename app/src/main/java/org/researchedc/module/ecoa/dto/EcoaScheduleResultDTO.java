package org.researchedc.module.ecoa.dto;

public class EcoaScheduleResultDTO {
    private EcoaScheduleDTO schedule;
    private EcoaAssignmentDTO assignment;
    private String participantEntryUrl;

    public EcoaScheduleDTO getSchedule() { return schedule; }
    public void setSchedule(EcoaScheduleDTO schedule) { this.schedule = schedule; }

    public EcoaAssignmentDTO getAssignment() { return assignment; }
    public void setAssignment(EcoaAssignmentDTO assignment) { this.assignment = assignment; }

    public String getParticipantEntryUrl() { return participantEntryUrl; }
    public void setParticipantEntryUrl(String participantEntryUrl) { this.participantEntryUrl = participantEntryUrl; }
}
