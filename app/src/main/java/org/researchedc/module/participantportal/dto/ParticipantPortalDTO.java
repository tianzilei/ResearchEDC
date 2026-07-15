package org.researchedc.module.participantportal.dto;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.module.participantaccess.dto.ParticipantBootstrapDTO;

public class ParticipantPortalDTO {
    private ParticipantBootstrapDTO participant;
    private ParticipantPortalSummaryDTO summary;
    private List<ParticipantPortalTaskDTO> tasks = new ArrayList<>();

    public ParticipantBootstrapDTO getParticipant() { return participant; }
    public void setParticipant(ParticipantBootstrapDTO participant) { this.participant = participant; }

    public ParticipantPortalSummaryDTO getSummary() { return summary; }
    public void setSummary(ParticipantPortalSummaryDTO summary) { this.summary = summary; }

    public List<ParticipantPortalTaskDTO> getTasks() { return tasks; }
    public void setTasks(List<ParticipantPortalTaskDTO> tasks) { this.tasks = tasks; }
}
