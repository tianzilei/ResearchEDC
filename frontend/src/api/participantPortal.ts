import { apiClient } from "@/api/client";
import type { ParticipantBootstrapDTO } from "@/api/participantAccess";

export type ParticipantPortalTaskType = "ECOA" | "CONSENT";

export interface ParticipantPortalSummaryDTO {
  totalTasks: number;
  questionnaireTasks: number;
  consentTasks: number;
  overdueTasks: number;
  actionableTasks: number;
}

export interface ParticipantPortalTaskDTO {
  id: string;
  type: ParticipantPortalTaskType;
  assignmentId: number;
  taskInstanceId: number | null;
  title: string;
  subtitle: string | null;
  description: string | null;
  status: string;
  dueAt: string | null;
  actionable: boolean;
  questionnaireAssignmentId: string | null;
  consentVersionLabel: string | null;
  consentBodyText: string | null;
}

export interface ParticipantPortalDTO {
  participant: ParticipantBootstrapDTO;
  summary: ParticipantPortalSummaryDTO;
  tasks: ParticipantPortalTaskDTO[];
}

export const participantPortalApi = {
  bootstrap(rawToken: string) {
    return apiClient.get<ParticipantPortalDTO>("/api/v1/participant-portal/public/bootstrap", { token: rawToken });
  },
};
