import { useAppMutation, useAppQuery, useQueryClient } from "@/hooks/useQuery";
import {
  ecoaApi,
  type CreateEcoaScheduleRequest,
  type EcoaAdherenceSummaryDTO,
  type EcoaAssignmentDTO,
  type EcoaScheduleDTO,
  type EcoaScheduleResultDTO,
  type RecordEcoaCompletionRequest,
} from "@/api/ecoa";

export type {
  CreateEcoaScheduleRequest,
  EcoaAdherenceSummaryDTO,
  EcoaAssignmentDTO,
  EcoaScheduleDTO,
  EcoaScheduleResultDTO,
  RecordEcoaCompletionRequest,
};

export function useEcoaSchedules(studyId: number | undefined) {
  return useAppQuery<EcoaScheduleDTO[]>({
    queryKey: ["ecoa", "schedules", studyId],
    queryFn: () => (studyId ? ecoaApi.listSchedules(studyId) : Promise.resolve([])),
    enabled: !!studyId,
    staleTime: 30 * 1000,
  });
}

export function useEcoaAssignments(studyId: number | undefined) {
  return useAppQuery<EcoaAssignmentDTO[]>({
    queryKey: ["ecoa", "assignments", studyId],
    queryFn: () => ecoaApi.listAssignments(studyId),
    enabled: !!studyId,
    staleTime: 15 * 1000,
  });
}

export function useEcoaAdherence(studyId: number | undefined) {
  return useAppQuery<EcoaAdherenceSummaryDTO>({
    queryKey: ["ecoa", "adherence", studyId],
    queryFn: () => ecoaApi.adherence(studyId),
    enabled: !!studyId,
    staleTime: 15 * 1000,
  });
}

export function useCreateEcoaSchedule(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<EcoaScheduleResultDTO, CreateEcoaScheduleRequest>({
    mutationFn: (body) => ecoaApi.createSchedule(body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["ecoa", "schedules", studyId] });
      qc.invalidateQueries({ queryKey: ["ecoa", "assignments", studyId] });
      qc.invalidateQueries({ queryKey: ["ecoa", "adherence", studyId] });
      qc.invalidateQueries({ queryKey: ["tasks"] });
      qc.invalidateQueries({ queryKey: ["participant-access"] });
    },
  });
}

export function useCompleteEcoaAssignment(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<EcoaAssignmentDTO, { assignmentId: number; request?: RecordEcoaCompletionRequest }>({
    mutationFn: ({ assignmentId, request }) => ecoaApi.recordCompletion(assignmentId, request ?? {}),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["ecoa", "assignments", studyId] });
      qc.invalidateQueries({ queryKey: ["ecoa", "adherence", studyId] });
      qc.invalidateQueries({ queryKey: ["tasks"] });
    },
  });
}

export function useCancelEcoaAssignment(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<EcoaAssignmentDTO, number>({
    mutationFn: (assignmentId) => ecoaApi.cancelAssignment(assignmentId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["ecoa", "assignments", studyId] });
      qc.invalidateQueries({ queryKey: ["ecoa", "adherence", studyId] });
      qc.invalidateQueries({ queryKey: ["tasks"] });
    },
  });
}
