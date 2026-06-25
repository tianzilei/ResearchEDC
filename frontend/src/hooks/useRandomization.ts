import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import type {
  SchemeDTO,
  SchemeSummaryDTO,
  AssignmentDTO,
  RandomizeRequest,
  UnblindingRequestDTO,
  AuditLogDTO,
} from "@/types/randomization";

const BASE = "/api/v1/randomization";

// === Schemes ===

export function useSchemes(studyId: number | undefined) {
  return useAppQuery<SchemeSummaryDTO[]>({
    queryKey: ["randomization", "schemes", studyId],
    queryFn: () =>
      studyId
        ? apiClient.get<SchemeSummaryDTO[]>(`${BASE}/schemes`, { studyId })
        : Promise.resolve([]),
    enabled: !!studyId,
  });
}

export function useScheme(id: number) {
  return useAppQuery<SchemeDTO>({
    queryKey: ["randomization", "scheme", id],
    queryFn: () => apiClient.get<SchemeDTO>(`${BASE}/schemes/${id}`),
    enabled: id > 0,
  });
}

export function useCreateScheme() {
  const qc = useQueryClient();
  return useAppMutation<SchemeDTO, SchemeDTO>({
    mutationFn: (dto) =>
      apiClient.post<SchemeDTO>(`${BASE}/schemes?userId=0`, dto),
    onSuccess: (data) => {
      void qc.invalidateQueries({ queryKey: ["randomization", "schemes", data.studyId] });
    },
  });
}

export function useUpdateScheme() {
  const qc = useQueryClient();
  return useAppMutation<SchemeDTO, SchemeDTO & { id: number }>({
    mutationFn: (dto) =>
      apiClient.put<SchemeDTO>(`${BASE}/schemes/${dto.id}?userId=0`, dto),
    onSuccess: (data) => {
      void qc.invalidateQueries({ queryKey: ["randomization", "schemes", data.studyId] });
      void qc.invalidateQueries({ queryKey: ["randomization", "scheme", data.id] });
    },
  });
}

export function useActivateScheme() {
  const qc = useQueryClient();
  return useAppMutation<undefined, number>({
    mutationFn: (id) =>
      apiClient.post<undefined>(`${BASE}/schemes/${id}/activate?userId=0`),
    onSuccess: () => { void qc.invalidateQueries({ queryKey: ["randomization"] }); },
  });
}

export function useCloseScheme() {
  const qc = useQueryClient();
  return useAppMutation<undefined, number>({
    mutationFn: (id) =>
      apiClient.post<undefined>(`${BASE}/schemes/${id}/close?userId=0`),
    onSuccess: () => { void qc.invalidateQueries({ queryKey: ["randomization"] }); },
  });
}

// === Assignment ===

export function useRandomize() {
  const qc = useQueryClient();
  return useAppMutation<AssignmentDTO, RandomizeRequest>({
    mutationFn: (req) =>
      apiClient.post<AssignmentDTO>(`${BASE}/randomize?userId=0`, req),
    onSuccess: () => { void qc.invalidateQueries({ queryKey: ["randomization"] }); },
  });
}

export function useAssignments(schemeId: number) {
  return useAppQuery<AssignmentDTO[]>({
    queryKey: ["randomization", "assignments", schemeId],
    queryFn: () => apiClient.get<AssignmentDTO[]>(`${BASE}/assignments`, { schemeId }),
    enabled: schemeId > 0,
  });
}

// === Unblinding ===

export function useUnblindingRequests(schemeId: number) {
  return useAppQuery<UnblindingRequestDTO[]>({
    queryKey: ["randomization", "unblinding", schemeId],
    queryFn: () => apiClient.get<UnblindingRequestDTO[]>(`${BASE}/unblinding/requests`, { schemeId }),
    enabled: schemeId > 0,
  });
}

export function usePendingUnblindingRequests() {
  return useAppQuery<UnblindingRequestDTO[]>({
    queryKey: ["randomization", "unblinding", "pending"],
    queryFn: () => apiClient.get<UnblindingRequestDTO[]>(`${BASE}/unblinding/pending`),
  });
}

export function useRequestUnblinding() {
  const qc = useQueryClient();
  return useAppMutation<UnblindingRequestDTO, { assignmentId: number; reason?: string }>({
    mutationFn: ({ assignmentId, reason }) =>
      apiClient.post<UnblindingRequestDTO>(
        `${BASE}/unblinding/request?assignmentId=${String(assignmentId)}&requestedBy=0${reason ? `&reason=${encodeURIComponent(reason)}` : ""}`,
      ),
    onSuccess: () => { void qc.invalidateQueries({ queryKey: ["randomization", "unblinding"] }); },
  });
}

// === Audit ===

export function useAuditLogs(schemeId: number) {
  return useAppQuery<AuditLogDTO[]>({
    queryKey: ["randomization", "audit", schemeId],
    queryFn: () => apiClient.get<AuditLogDTO[]>(`${BASE}/audit`, { schemeId }),
    enabled: schemeId > 0,
  });
}
