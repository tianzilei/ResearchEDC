import { apiClient } from "@/api/client";
import type { StudyDetail } from "@/types/study";

export interface StudyListItem {
  studyId: number;
  name: string;
  uniqueIdentifier: string | null;
  phase: string | null;
  principalInvestigator: string | null;
  sponsor: string | null;
  dateCreated: string;
  expectedTotalEnrollment: number | null;
  site: boolean;
  parentStudyId: number | null;
}

export type StudyCreateRequest = Record<string, unknown>;
export type StudyUpdateRequest = Record<string, unknown>;

export interface StudyCreateResponse {
  id?: number;
  studyId?: number;
}

export const studyApi = {
  list() {
    return apiClient.get<StudyListItem[]>("/api/v1/studies");
  },

  getDetail(studyId: number) {
    return apiClient.get<StudyDetail>(`/api/v1/studies/${studyId}`);
  },

  create(request: StudyCreateRequest) {
    return apiClient.post<StudyCreateResponse>("/api/v1/studies", request);
  },

  update(studyId: number, request: StudyUpdateRequest) {
    return apiClient.put<void>(`/api/v1/studies/${studyId}`, request);
  },
};
