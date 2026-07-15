import { apiClient } from "@/api/client";

export type TaskStatus = "PENDING" | "DUE" | "OVERDUE" | "COMPLETED" | "CANCELLED" | "EXPIRED";
export type TaskTargetType = "STUDY" | "SUBJECT" | "STUDY_EVENT" | "EVENT_CRF" | "ECOA_ASSIGNMENT" | "CONSENT_ASSIGNMENT" | "EXPORT_JOB" | "IMPORT_JOB" | "SYSTEM";

export interface TaskTemplateDTO {
  id: number;
  studyId: number;
  name: string;
  description: string;
  taskType: string;
  defaultDueDays: number | null;
  active: boolean;
  createdBy: number | null;
  createdDate: string;
}

export interface TaskInstanceDTO {
  id: number;
  templateId: number | null;
  studyId: number;
  assignedTo: number | null;
  title: string;
  description: string;
  targetType: TaskTargetType;
  targetId: number | null;
  status: TaskStatus;
  dueDate: string | null;
  createdBy: number | null;
  createdDate: string;
  completedBy: number | null;
  completedDate: string | null;
  cancelledBy: number | null;
  cancelledDate: string | null;
  lastReminderDate: string | null;
  reminderCount: number;
}

export interface CreateTaskRequest {
  templateId?: number;
  studyId: number;
  assignedTo?: number;
  title: string;
  description?: string;
  targetType?: TaskTargetType;
  targetId?: number;
  dueDate?: string;
}

export interface CreateTaskTemplateRequest {
  studyId: number;
  name: string;
  description?: string;
  taskType: string;
  defaultDueDays?: number;
}

export interface TaskFilters {
  studyId?: number;
  status?: TaskStatus;
  assignedToMe?: boolean;
}

export const taskApi = {
  listTasks(filters: TaskFilters) {
    const params: Record<string, string | number | boolean> = {};
    if (filters.studyId) params.studyId = filters.studyId;
    if (filters.status) params.status = filters.status;
    if (filters.assignedToMe) params.assignedToMe = true;
    return apiClient.get<TaskInstanceDTO[]>("/api/v1/tasks", params);
  },

  createTask(request: CreateTaskRequest) {
    return apiClient.post<TaskInstanceDTO>("/api/v1/tasks", request);
  },

  completeTask(taskId: number) {
    return apiClient.post<TaskInstanceDTO>(`/api/v1/tasks/${taskId}/complete`);
  },

  cancelTask(taskId: number) {
    return apiClient.post<TaskInstanceDTO>(`/api/v1/tasks/${taskId}/cancel`);
  },

  dispatchReminder(taskId: number) {
    return apiClient.post<TaskInstanceDTO>(`/api/v1/tasks/${taskId}/reminders`);
  },

  listTemplates(studyId: number) {
    return apiClient.get<TaskTemplateDTO[]>("/api/v1/tasks/templates", { studyId });
  },

  createTemplate(request: CreateTaskTemplateRequest) {
    return apiClient.post<TaskTemplateDTO>("/api/v1/tasks/templates", request);
  },
};
