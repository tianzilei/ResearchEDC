import { apiClient } from "@/api/client";

export interface StudySubjectDTO {
  studySubjectId: number;
  studyId: number;
  subjectId: number;
  label: string;
  secondaryLabel: string | null;
  ocOid: string | null;
  enrollmentDate: string | null;
  dateCreated: string;
  dateUpdated?: string | null;
  status?: string;
  subjectUniqueIdentifier?: string | null;
}

export interface SubjectDTO {
  subjectId: number;
  uniqueIdentifier: string;
  dateOfBirth: string | null;
  gender: string | null;
  dobCollected?: boolean | null;
  dateCreated: string;
}

export interface CreateSubjectRequest {
  uniqueIdentifier: string;
  gender?: string | null;
  dateOfBirth?: string | null;
}

export interface EnrollSubjectRequest {
  studyId: number;
  subjectId: number;
  label: string;
  enrollmentDate?: string | null;
  eventDefinitionId?: number | null;
}

export interface SignSubjectRequest {
  reason: string;
  password?: string;
}

export interface ReassignSubjectRequest {
  studyId: number;
}

export const subjectApi = {
  listByStudy(studyId: number) {
    return apiClient.get<StudySubjectDTO[]>("/api/v1/subjects/by-study", { studyId });
  },

  getEnrollment(studySubjectId: number) {
    return apiClient.get<StudySubjectDTO>(`/api/v1/subjects/enrollment/${studySubjectId}`);
  },

  getSubject(subjectId: number) {
    return apiClient.get<SubjectDTO>(`/api/v1/subjects/${subjectId}`);
  },

  createSubject(request: CreateSubjectRequest) {
    return apiClient.post<SubjectDTO>("/api/v1/subjects", request);
  },

  enrollSubject(request: EnrollSubjectRequest) {
    return apiClient.post<StudySubjectDTO>("/api/v1/subjects/enroll", request);
  },

  signStudySubject(studySubjectId: number, request: SignSubjectRequest) {
    return apiClient.post<void>(`/api/v1/subjects/${studySubjectId}/sign`, request);
  },

  updateSubject(subjectId: number, request: Partial<SubjectDTO>) {
    return apiClient.put<void>(`/api/v1/subjects/${subjectId}`, request);
  },

  reassignStudySubject(studySubjectId: number, request: ReassignSubjectRequest) {
    return apiClient.put<void>(`/api/v1/subjects/${studySubjectId}/reassign`, request);
  },
};
