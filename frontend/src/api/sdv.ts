import { apiClient } from "@/api/client";

export type SdvStatus = "PENDING" | "VERIFIED" | "REQUIRES_CHANGES";

export interface SdvReviewDTO {
  id: number;
  studyId: number;
  eventCrfId: number;
  studySubjectId: number | null;
  status: SdvStatus;
  reviewNotes: string | null;
  reviewedBy: number | null;
  reviewedDate: string | null;
  createdDate: string;
  updatedDate: string | null;
}

export interface CreateSdvReviewRequest {
  eventCrfId: number;
}

export interface UpdateSdvReviewRequest {
  status: SdvStatus;
  reviewNotes?: string;
}

export const sdvApi = {
  listReviews(studyId: number, status?: SdvStatus) {
    return apiClient.get<SdvReviewDTO[]>("/api/v1/sdv/reviews", status ? { studyId, status } : { studyId });
  },

  createReview(request: CreateSdvReviewRequest) {
    return apiClient.post<SdvReviewDTO>("/api/v1/sdv/reviews", request);
  },

  updateReview(reviewId: number, request: UpdateSdvReviewRequest) {
    return apiClient.post<SdvReviewDTO>(`/api/v1/sdv/reviews/${reviewId}`, request);
  },
};
