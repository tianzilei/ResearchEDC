import { apiClient } from "@/api/client";
import type { components } from "@/api/generated";

export type ExportJobDTO = components["schemas"]["ExportJobDTO"];
export type CreateExportJobRequest = components["schemas"]["CreateExportJobRequest"];
export type ExportFormat = components["schemas"]["ExportFormat"];
export type ExportJobStatus = components["schemas"]["ExportJobStatus"];
export type OdmContractVersion = components["schemas"]["OdmContractVersion"];

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
