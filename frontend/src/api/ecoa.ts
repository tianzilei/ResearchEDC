import { apiClient } from "@/api/client";

export type EcoaAssignmentStatus =
  | "PENDING"
  | "IN_PROGRESS"
  | "SUBMITTED"
  | "REVIEWED"
  | "OVERDUE"
  | "CANCELLED";

export interface CreateEcoaScheduleRequest {
  studyId: number;
  studySubjectId: number;
  studyEventId?: number;
  questionnaireVersionId: string;
  title: string;
  description?: string;
  dueAt: string;
  windowOpensAt?: string;
  windowClosesAt?: string;
}

export interface EcoaScheduleDTO {
  id: number;
  studyId: number;
  studySubjectId: number;
  studyEventId: number | null;
  questionnaireVersionId: string;
  title: string;
  description: string;
  dueAt: string;
  windowOpensAt: string | null;
  windowClosesAt: string | null;
  createdBy: number | null;
  createdDate: string;
}

export interface EcoaAssignmentDTO {
  id: number;
  scheduleId: number;
  studyId: number;
  studySubjectId: number;
  participantAccountId: number;
  participantTokenId: number | null;
  taskInstanceId: number | null;
  questionnaireAssignmentId: string | null;
  status: EcoaAssignmentStatus;
  dueAt: string;
  windowOpensAt: string | null;
  windowClosesAt: string | null;
  entryUrl: string | null;
  completedAt: string | null;
  scoreSummary: string | null;
  createdBy: number | null;
  createdDate: string;
  updatedDate: string | null;
}

export interface EcoaAdherenceSummaryDTO {
  total: number;
  pending: number;
  inProgress: number;
  completed: number;
  overdue: number;
  completionRate: number;
}

export interface EcoaScheduleResultDTO {
  schedule: EcoaScheduleDTO;
  assignment: EcoaAssignmentDTO;
  participantEntryUrl: string;
}

export interface RecordEcoaCompletionRequest {
  questionnaireAssignmentId?: string;
  completedAt?: string;
  scoreSummary?: string;
}

export const ecoaApi = {
  listSchedules(studyId: number) {
    return apiClient.get<EcoaScheduleDTO[]>("/api/v1/ecoa/schedules", { studyId });
  },

  createSchedule(request: CreateEcoaScheduleRequest) {
    return apiClient.post<EcoaScheduleResultDTO>("/api/v1/ecoa/schedules", request);
  },

  listAssignments(studyId?: number) {
    return apiClient.get<EcoaAssignmentDTO[]>("/api/v1/ecoa/assignments", studyId ? { studyId } : undefined);
  },

  adherence(studyId?: number) {
    return apiClient.get<EcoaAdherenceSummaryDTO>("/api/v1/ecoa/adherence", studyId ? { studyId } : undefined);
  },

  recordCompletion(assignmentId: number, request: RecordEcoaCompletionRequest = {}) {
    return apiClient.post<EcoaAssignmentDTO>(`/api/v1/ecoa/assignments/${assignmentId}/complete`, request);
  },

  cancelAssignment(assignmentId: number) {
    return apiClient.post<EcoaAssignmentDTO>(`/api/v1/ecoa/assignments/${assignmentId}/cancel`);
  },

  listParticipantAssignments(rawToken: string) {
    return apiClient.get<EcoaAssignmentDTO[]>("/api/v1/ecoa/public/assignments", { token: rawToken });
  },

  completeParticipantAssignment(assignmentId: number, rawToken: string, request: RecordEcoaCompletionRequest = {}) {
    return apiClient.post<EcoaAssignmentDTO>(
      `/api/v1/ecoa/public/assignments/${assignmentId}/complete`,
      request,
      { token: rawToken },
    );
  },
};
