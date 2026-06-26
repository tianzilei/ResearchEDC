import { useMemo } from "react";
import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { exportApi, type ExportJobDTO, type CreateExportJobRequest, type ExportFormat, type ExportJobStatus, type OdmContractVersion } from "@/api/exports";
import { apiClient } from "@/api/client";

export type { ExportJobDTO, CreateExportJobRequest, ExportFormat, ExportJobStatus, OdmContractVersion };

interface ExportFilters {
  status?: ExportJobStatus;
  exportFormat?: ExportFormat;
  odmContractVersion?: OdmContractVersion;
}

export function useExportJobs(studyId: number | undefined, filters?: ExportFilters) {
  const params = useMemo(() => {
    const p: Record<string, string | number> = {};
    if (studyId) p.studyId = studyId;
    if (filters?.status) p.status = filters.status;
    if (filters?.exportFormat) p.exportFormat = filters.exportFormat;
    if (filters?.odmContractVersion) p.odmContractVersion = filters.odmContractVersion;
    return p;
  }, [studyId, filters?.status, filters?.exportFormat, filters?.odmContractVersion]);

  return useAppQuery<ExportJobDTO[]>({
    queryKey: ["exports", studyId, params],
    queryFn: () =>
      studyId
        ? apiClient.get<ExportJobDTO[]>("/api/v1/exports", params)
        : Promise.resolve([]),
    enabled: !!studyId,
  });
}

export function useCreateExportJob(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<ExportJobDTO, CreateExportJobRequest>({
    mutationFn: (body) => exportApi.createJob(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["exports", studyId] }),
  });
}

export function useCancelExportJob(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<ExportJobDTO, number>({
    mutationFn: (id) => exportApi.cancelJob(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["exports", studyId] }),
  });
}

export function useRetryExportJob(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<ExportJobDTO, number>({
    mutationFn: (id) => exportApi.retryJob(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["exports", studyId] }),
  });
}
