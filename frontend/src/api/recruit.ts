import { apiClient } from "@/api/client";
import type { StudySubjectDTO, SubjectDTO } from "@/api/subjects";

export type CandidateStatus = "NEW" | "PRESCREENED" | "ELIGIBLE" | "INELIGIBLE" | "CONVERTED" | "REJECTED";
export type EligibilityDecision = "ELIGIBLE" | "INELIGIBLE" | "NEEDS_REVIEW";

export interface CandidateDTO {
  id: number;
  studyId: number;
  candidateCode: string;
  displayName: string;
  contactEmail: string;
  contactPhone: string;
  source: string;
  status: CandidateStatus;
  notes: string;
  createdBy: number | null;
  createdDate: string;
  updatedDate: string | null;
  convertedSubjectId: number | null;
  convertedStudySubjectId: number | null;
}

export interface PrescreenResultDTO {
  id: number;
  candidateId: number;
  studyId: number;
  decision: EligibilityDecision;
  score: number | null;
  criteriaSummary: string;
  reviewNotes: string;
  reviewedBy: number | null;
  reviewedDate: string;
}

export interface CreateCandidateRequest {
  studyId: number;
  candidateCode: string;
  displayName?: string;
  contactEmail?: string;
  contactPhone?: string;
  source?: string;
  notes?: string;
}

export interface RecordPrescreenRequest {
  decision: EligibilityDecision;
  score?: number;
  criteriaSummary?: string;
  reviewNotes?: string;
}

export interface ConvertCandidateRequest {
  subjectUniqueIdentifier?: string;
  studySubjectLabel?: string;
  gender?: string;
  dateOfBirth?: string;
  enrollmentDate?: string;
}

export interface ConvertCandidateResultDTO {
  candidate: CandidateDTO;
  subject: SubjectDTO;
  studySubject: StudySubjectDTO;
}

export const recruitApi = {
  listCandidates(studyId: number, status?: CandidateStatus) {
    return apiClient.get<CandidateDTO[]>("/api/v1/recruit/candidates", status ? { studyId, status } : { studyId });
  },

  createCandidate(request: CreateCandidateRequest) {
    return apiClient.post<CandidateDTO>("/api/v1/recruit/candidates", request);
  },

  listPrescreens(candidateId: number) {
    return apiClient.get<PrescreenResultDTO[]>(`/api/v1/recruit/candidates/${candidateId}/prescreens`);
  },

  recordPrescreen(candidateId: number, request: RecordPrescreenRequest) {
    return apiClient.post<PrescreenResultDTO>(`/api/v1/recruit/candidates/${candidateId}/prescreens`, request);
  },

  rejectCandidate(candidateId: number, reason?: string) {
    return apiClient.post<CandidateDTO>(`/api/v1/recruit/candidates/${candidateId}/reject`, { reason: reason ?? "" });
  },

  convertCandidate(candidateId: number, request: ConvertCandidateRequest) {
    return apiClient.post<ConvertCandidateResultDTO>(`/api/v1/recruit/candidates/${candidateId}/convert`, request);
  },
};
