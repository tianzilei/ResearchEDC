import { useMutation, useQueryClient } from "@tanstack/react-query";
import { fhirApi, type CreateFhirConnectorRequest, type FhirConnectorDTO, type FhirImportRecordDTO, type FhirImportStatus, type ReconcileFhirRecordRequest, type SubmitFhirResourceRequest } from "@/api/fhir";
import { useAppQuery } from "@/hooks/useQuery";

export type { FhirConnectorDTO, FhirImportRecordDTO, FhirImportStatus };

export function useFhirConnectors(studyId: number | undefined) {
  return useAppQuery<FhirConnectorDTO[]>({
    queryKey: ["fhir", "connectors", studyId],
    queryFn: () => fhirApi.listConnectors(studyId ?? 0),
    enabled: !!studyId,
  });
}

export function useFhirRecords(studyId: number | undefined, status?: FhirImportStatus) {
  return useAppQuery<FhirImportRecordDTO[]>({
    queryKey: ["fhir", "records", studyId, status],
    queryFn: () => fhirApi.listRecords(studyId ?? 0, status),
    enabled: !!studyId,
  });
}

export function useCreateFhirConnector() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateFhirConnectorRequest) => fhirApi.createConnector(request),
    onSuccess: (_result, request) => {
      void queryClient.invalidateQueries({ queryKey: ["fhir", "connectors", request.studyId] });
    },
  });
}

export function useSubmitFhirResource() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: SubmitFhirResourceRequest) => fhirApi.submitResource(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["fhir", "records"] });
    },
  });
}

export function useReconcileFhirRecord() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ recordId, request }: { recordId: number; request: ReconcileFhirRecordRequest }) =>
      fhirApi.reconcile(recordId, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["fhir", "records"] });
    },
  });
}
