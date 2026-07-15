import { apiClient } from "@/api/client";

export type ConsentVersionStatus = "DRAFT" | "PUBLISHED" | "RETIRED";
export type ConsentAssignmentStatus =
  | "ASSIGNED"
  | "PARTICIPANT_SIGNED"
  | "COUNTERSIGNED"
  | "SUPERSEDED"
  | "CANCELLED";

export interface CreateConsentTemplateRequest {
  studyId: number;
  code: string;
  name: string;
  description?: string;
}

export interface CreateConsentVersionRequest {
  versionLabel: string;
  bodyText: string;
}

export interface AssignConsentRequest {
  studySubjectId: number;
  consentVersionId: number;
  dueAt?: string;
}

export interface SignConsentRequest {
  participantName: string;
  signature: string;
  evidence?: string;
}

export interface CountersignConsentRequest {
  countersignature: string;
}

export interface ConsentTemplateDTO {
  id: number;
  studyId: number;
  code: string;
  name: string;
  description: string;
  active: boolean;
  createdBy: number | null;
  createdDate: string;
}

export interface ConsentVersionDTO {
  id: number;
  templateId: number;
  studyId: number;
  versionLabel: string;
  bodyText: string;
  status: ConsentVersionStatus;
  publishedDate: string | null;
  createdBy: number | null;
  createdDate: string;
}

export interface ConsentAssignmentDTO {
  id: number;
  studyId: number;
  studySubjectId: number;
  consentVersionId: number;
  participantAccountId: number;
  participantTokenId: number | null;
  taskInstanceId: number | null;
  status: ConsentAssignmentStatus;
  dueAt: string | null;
  entryUrl: string | null;
  participantName: string | null;
  participantSignedAt: string | null;
  countersignedBy: number | null;
  countersignedAt: string | null;
  artifactName: string | null;
  createdBy: number | null;
  createdDate: string;
  updatedDate: string | null;
}

export interface ConsentAssignmentResultDTO {
  assignment: ConsentAssignmentDTO;
  participantEntryUrl: string;
}

export interface ParticipantConsentDTO {
  assignment: ConsentAssignmentDTO;
  version: ConsentVersionDTO;
  template: ConsentTemplateDTO;
}

export interface ConsentArtifactDTO {
  artifactName: string;
  contentType: string;
  content: string;
}

export const econsentApi = {
  listTemplates(studyId: number) {
    return apiClient.get<ConsentTemplateDTO[]>("/api/v1/econsent/templates", { studyId });
  },

  createTemplate(request: CreateConsentTemplateRequest) {
    return apiClient.post<ConsentTemplateDTO>("/api/v1/econsent/templates", request);
  },

  listVersions(templateId: number) {
    return apiClient.get<ConsentVersionDTO[]>(`/api/v1/econsent/templates/${templateId}/versions`);
  },

  createVersion(templateId: number, request: CreateConsentVersionRequest) {
    return apiClient.post<ConsentVersionDTO>(`/api/v1/econsent/templates/${templateId}/versions`, request);
  },

  publishVersion(versionId: number) {
    return apiClient.post<ConsentVersionDTO>(`/api/v1/econsent/versions/${versionId}/publish`);
  },

  listAssignments(studyId: number) {
    return apiClient.get<ConsentAssignmentDTO[]>("/api/v1/econsent/assignments", { studyId });
  },

  assignConsent(request: AssignConsentRequest) {
    return apiClient.post<ConsentAssignmentResultDTO>("/api/v1/econsent/assignments", request);
  },

  countersign(assignmentId: number, request: CountersignConsentRequest) {
    return apiClient.post<ConsentAssignmentDTO>(`/api/v1/econsent/assignments/${assignmentId}/countersign`, request);
  },

  artifact(assignmentId: number) {
    return apiClient.get<ConsentArtifactDTO>(`/api/v1/econsent/assignments/${assignmentId}/artifact`);
  },

  listParticipantConsents(rawToken: string) {
    return apiClient.get<ParticipantConsentDTO[]>("/api/v1/econsent/public/consents", { token: rawToken });
  },

  signParticipantConsent(assignmentId: number, rawToken: string, request: SignConsentRequest) {
    return apiClient.post<ConsentAssignmentDTO>(
      `/api/v1/econsent/public/assignments/${assignmentId}/sign`,
      request,
      { token: rawToken },
    );
  },
};
