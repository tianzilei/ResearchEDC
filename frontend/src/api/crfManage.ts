import { apiClient } from "@/api/client";

export interface CrfManageItem {
  crfId: number;
  name: string;
  description: string | null;
  ocOid: string | null;
  statusId: number | null;
  dateCreated: string | null;
}

export interface CrfVersionManageItem {
  crfVersionId: number;
  crfId: number;
  name: string;
  description: string | null;
  revisionNotes: string | null;
  statusId: number | null;
  dateCreated: string | null;
}

export interface CreateCrfRequest {
  name: string;
  description?: string;
}

export interface CreateCrfVersionRequest {
  name: string;
  description?: string;
  revisionNotes?: string;
}

export const crfManageApi = {
  listCrfs() {
    return apiClient.get<CrfManageItem[]>("/api/v1/crfs/manage");
  },

  listVersions(crfId: number) {
    return apiClient.get<CrfVersionManageItem[]>(`/api/v1/crfs/manage/${crfId}/versions`);
  },

  createCrf(request: CreateCrfRequest) {
    return apiClient.post<CrfManageItem>("/api/v1/crfs/manage", request);
  },

  createVersion(crfId: number, request: CreateCrfVersionRequest) {
    return apiClient.post<CrfVersionManageItem>(`/api/v1/crfs/manage/${crfId}/versions`, request);
  },

  deleteVersion(versionId: number) {
    return apiClient.delete<void>(`/api/v1/crfs/manage/versions/${versionId}`);
  },
};
