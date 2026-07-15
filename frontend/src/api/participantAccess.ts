import { apiClient } from "@/api/client";

export type ParticipantAccountStatus = "ACTIVE" | "DISABLED" | "WITHDRAWN";
export type ParticipantAccessTokenStatus = "ACTIVE" | "USED" | "REVOKED" | "EXPIRED";

export interface ParticipantAccountDTO {
  id: number;
  studyId: number;
  studySubjectId: number;
  subjectId: number | null;
  displayLabel: string;
  preferredLocale: string | null;
  status: ParticipantAccountStatus;
  createdBy: number | null;
  createdDate: string;
  updatedDate: string | null;
}

export interface ParticipantAccessTokenDTO {
  id: number;
  participantAccountId: number;
  studyId: number;
  studySubjectId: number;
  scope: string;
  status: ParticipantAccessTokenStatus;
  issuedBy: number | null;
  issuedDate: string;
  expiresAt: string;
  lastUsedAt: string | null;
  revokedBy: number | null;
  revokedDate: string | null;
  revocationReason: string | null;
}

export interface IssuedParticipantTokenDTO {
  token: ParticipantAccessTokenDTO;
  rawToken: string;
  entryUrl: string;
}

export interface ParticipantBootstrapDTO {
  tokenId: number;
  participantAccountId: number;
  studyId: number;
  studySubjectId: number;
  displayLabel: string;
  preferredLocale: string | null;
  scope: string;
  expiresAt: string;
}

export interface CreateParticipantAccountRequest {
  studySubjectId: number;
  displayLabel?: string;
  preferredLocale?: string;
}

export interface IssueParticipantTokenRequest {
  participantAccountId: number;
  expiresInHours?: number;
  scope?: string;
}

export const participantAccessApi = {
  listAccounts(studyId: number) {
    return apiClient.get<ParticipantAccountDTO[]>("/api/v1/participant-access/accounts", { studyId });
  },

  createAccount(request: CreateParticipantAccountRequest) {
    return apiClient.post<ParticipantAccountDTO>("/api/v1/participant-access/accounts", request);
  },

  listTokens(accountId: number) {
    return apiClient.get<ParticipantAccessTokenDTO[]>(`/api/v1/participant-access/accounts/${accountId}/tokens`);
  },

  issueToken(request: IssueParticipantTokenRequest) {
    return apiClient.post<IssuedParticipantTokenDTO>("/api/v1/participant-access/tokens", request);
  },

  revokeToken(tokenId: number, reason?: string) {
    return apiClient.post<ParticipantAccessTokenDTO>(`/api/v1/participant-access/tokens/${tokenId}/revoke`, {
      reason: reason ?? "",
    });
  },

  bootstrap(rawToken: string) {
    return apiClient.get<ParticipantBootstrapDTO>("/api/v1/participant-access/public/bootstrap", { token: rawToken });
  },
};
