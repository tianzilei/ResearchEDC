import { apiClient } from "@/api/client";

export interface ExportJobDTO {
  id: number;
  studyId: number;
  name?: string;
  exportFormat: string;
  odmContractVersion?: string;
  status: string;
  requestedBy?: number;
  requestedDate: string;
  completedDate?: string | null;
  filePath?: string | null;
  fileSize?: number | null;
  errorMessage?: string | null;
  failureCode?: string | null;
  retryable?: boolean | null;
  criteriaJson?: string | null;
  retryCount?: number | null;
}

export interface CreateExportJobRequest {
  studyId: number;
  name?: string;
  exportFormat: string;
  odmContractVersion?: string;
  requestedBy?: number;
  criteriaJson?: string;
}

export const exportApi = {
  listJobs(studyId: number) {
    return apiClient.get<ExportJobDTO[]>("/api/v1/exports", { studyId });
  },

  createJob(request: CreateExportJobRequest) {
    return apiClient.post<ExportJobDTO>("/api/v1/exports", request);
  },

  cancelJob(jobId: number) {
    return apiClient.post<ExportJobDTO>(`/api/v1/exports/${jobId}/cancel`);
  },

  retryJob(jobId: number) {
    return apiClient.post<ExportJobDTO>(`/api/v1/exports/${jobId}/retry`);
  },
};
