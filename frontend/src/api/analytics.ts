import { apiClient } from "@/api/client";

export interface AnalyticsMetricDTO {
  key: string;
  label: string;
  value: number;
  unit: string;
}

export interface AnalyticsDashboardDTO {
  studyId: number;
  enrollment: AnalyticsMetricDTO[];
  participantWork: AnalyticsMetricDTO[];
  operations: AnalyticsMetricDTO[];
}

export const analyticsApi = {
  dashboard(studyId: number) {
    return apiClient.get<AnalyticsDashboardDTO>("/api/v1/analytics/dashboard", { studyId });
  },
};
