import { apiClient } from "@/api/client";

export interface AuditLogEntry {
  id: number;
  studyId: number | null;
  eventType: string;
  entityType: string | null;
  entityId: number | null;
  entityLabel: string | null;
  oldValue: string | null;
  newValue: string | null;
  performedBy: number | null;
  performedDate: string;
  details: string | null;
  sourceModule: string | null;
}

interface PageResponse<T> {
  content?: T[];
}

export const auditApi = {
  listLogs() {
    return apiClient.get<PageResponse<AuditLogEntry> | AuditLogEntry[]>("/api/v1/audit");
  },
};
