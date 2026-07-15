import { useMutation, useQueryClient } from "@tanstack/react-query";
import { sdvApi, type CreateSdvReviewRequest, type SdvReviewDTO, type SdvStatus, type UpdateSdvReviewRequest } from "@/api/sdv";
import { useAppQuery } from "@/hooks/useQuery";

export type { CreateSdvReviewRequest, SdvReviewDTO, SdvStatus, UpdateSdvReviewRequest };

export function useSdvReviews(studyId: number | undefined, status?: SdvStatus) {
  return useAppQuery<SdvReviewDTO[]>({
    queryKey: ["sdv", "reviews", studyId, status],
    queryFn: () => sdvApi.listReviews(studyId ?? 0, status),
    enabled: !!studyId,
  });
}

export function useCreateSdvReview() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateSdvReviewRequest) => sdvApi.createReview(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["sdv", "reviews"] });
    },
  });
}

export function useUpdateSdvReview() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ reviewId, request }: { reviewId: number; request: UpdateSdvReviewRequest }) =>
      sdvApi.updateReview(reviewId, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["sdv", "reviews"] });
    },
  });
}
