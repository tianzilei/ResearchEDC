import { useMutation, useQueryClient } from "@tanstack/react-query";
import { recruitApi, type CandidateDTO, type CandidateStatus, type ConvertCandidateRequest, type CreateCandidateRequest, type RecordPrescreenRequest } from "@/api/recruit";
import { useAppQuery } from "@/hooks/useQuery";

export type { CandidateDTO, CandidateStatus, ConvertCandidateRequest, CreateCandidateRequest, RecordPrescreenRequest };

export function useRecruitCandidates(studyId: number | undefined, status?: CandidateStatus) {
  return useAppQuery<CandidateDTO[]>({
    queryKey: ["recruit", "candidates", studyId, status],
    queryFn: () => recruitApi.listCandidates(studyId ?? 0, status),
    enabled: !!studyId,
  });
}

export function useCreateCandidate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateCandidateRequest) => recruitApi.createCandidate(request),
    onSuccess: (_result, request) => {
      void queryClient.invalidateQueries({ queryKey: ["recruit", "candidates", request.studyId] });
    },
  });
}

export function useRecordPrescreen() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ candidateId, request }: { candidateId: number; request: RecordPrescreenRequest }) =>
      recruitApi.recordPrescreen(candidateId, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["recruit", "candidates"] });
    },
  });
}

export function useRejectCandidate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ candidateId, reason }: { candidateId: number; reason?: string }) =>
      recruitApi.rejectCandidate(candidateId, reason),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["recruit", "candidates"] });
    },
  });
}

export function useConvertCandidate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ candidateId, request }: { candidateId: number; request: ConvertCandidateRequest }) =>
      recruitApi.convertCandidate(candidateId, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["recruit", "candidates"] });
      void queryClient.invalidateQueries({ queryKey: ["subjects"] });
    },
  });
}
