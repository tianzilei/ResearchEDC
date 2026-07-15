import { useAppMutation, useAppQuery, useQueryClient } from "@/hooks/useQuery";
import {
  participantAccessApi,
  type CreateParticipantAccountRequest,
  type IssueParticipantTokenRequest,
  type IssuedParticipantTokenDTO,
  type ParticipantAccessTokenDTO,
  type ParticipantAccountDTO,
} from "@/api/participantAccess";

export type {
  CreateParticipantAccountRequest,
  IssueParticipantTokenRequest,
  IssuedParticipantTokenDTO,
  ParticipantAccessTokenDTO,
  ParticipantAccountDTO,
};

export function useParticipantAccounts(studyId: number | undefined) {
  return useAppQuery<ParticipantAccountDTO[]>({
    queryKey: ["participant-access", "accounts", studyId],
    queryFn: () =>
      studyId
        ? participantAccessApi.listAccounts(studyId)
        : Promise.resolve([]),
    enabled: !!studyId,
    staleTime: 30 * 1000,
  });
}

export function useParticipantTokens(accountId: number | undefined) {
  return useAppQuery<ParticipantAccessTokenDTO[]>({
    queryKey: ["participant-access", "tokens", accountId],
    queryFn: () =>
      accountId
        ? participantAccessApi.listTokens(accountId)
        : Promise.resolve([]),
    enabled: !!accountId,
    staleTime: 15 * 1000,
  });
}

export function useCreateParticipantAccount(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<ParticipantAccountDTO, CreateParticipantAccountRequest>({
    mutationFn: (body) => participantAccessApi.createAccount(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["participant-access", "accounts", studyId] }),
  });
}

export function useIssueParticipantToken(accountId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<IssuedParticipantTokenDTO, IssueParticipantTokenRequest>({
    mutationFn: (body) => participantAccessApi.issueToken(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["participant-access", "tokens", accountId] }),
  });
}

export function useRevokeParticipantToken(accountId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<ParticipantAccessTokenDTO, { tokenId: number; reason?: string }>({
    mutationFn: ({ tokenId, reason }) => participantAccessApi.revokeToken(tokenId, reason),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["participant-access", "tokens", accountId] }),
  });
}
