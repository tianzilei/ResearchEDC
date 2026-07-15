package org.researchedc.module.analytics.dto;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsDashboardDTO {
    private Integer studyId;
    private List<AnalyticsMetricDTO> enrollment = new ArrayList<>();
    private List<AnalyticsMetricDTO> participantWork = new ArrayList<>();
    private List<AnalyticsMetricDTO> operations = new ArrayList<>();

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public List<AnalyticsMetricDTO> getEnrollment() { return enrollment; }
    public void setEnrollment(List<AnalyticsMetricDTO> enrollment) { this.enrollment = enrollment; }

    public List<AnalyticsMetricDTO> getParticipantWork() { return participantWork; }
    public void setParticipantWork(List<AnalyticsMetricDTO> participantWork) {
        this.participantWork = participantWork;
    }

    public List<AnalyticsMetricDTO> getOperations() { return operations; }
    public void setOperations(List<AnalyticsMetricDTO> operations) { this.operations = operations; }
}
