package org.researchedc.module.participantportal.dto;

public class ParticipantPortalSummaryDTO {
    private int totalTasks;
    private int questionnaireTasks;
    private int consentTasks;
    private int overdueTasks;
    private int actionableTasks;

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getQuestionnaireTasks() { return questionnaireTasks; }
    public void setQuestionnaireTasks(int questionnaireTasks) { this.questionnaireTasks = questionnaireTasks; }

    public int getConsentTasks() { return consentTasks; }
    public void setConsentTasks(int consentTasks) { this.consentTasks = consentTasks; }

    public int getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }

    public int getActionableTasks() { return actionableTasks; }
    public void setActionableTasks(int actionableTasks) { this.actionableTasks = actionableTasks; }
}
