import { useAppQuery, useAppMutation } from "@/hooks/useQuery";
import { useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/api/client";
import type { DiscrepancyNoteDTO, CreateDiscrepancyNoteRequest } from "@/types/discrepancy";

export function useEventCrfNotes(eventCrfId: number | undefined) {
  return useAppQuery<DiscrepancyNoteDTO[]>({
    queryKey: ["discrepancy-notes", eventCrfId],
    queryFn: () =>
      eventCrfId
        ? apiClient.get<DiscrepancyNoteDTO[]>("/api/v1/discrepancy-notes", {
            eventCrfId,
          })
        : Promise.resolve([]),
    enabled: !!eventCrfId,
  });
}

export function useCreateNote() {
  const queryClient = useQueryClient();

  return useAppMutation<DiscrepancyNoteDTO, CreateDiscrepancyNoteRequest>({
    mutationFn: (data) => apiClient.post("/api/v1/discrepancy-notes", data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["discrepancy-notes"] });
    },
  });
}

export function useResolveNote() {
  const queryClient = useQueryClient();

  return useAppMutation<DiscrepancyNoteDTO, number>({
    mutationFn: (noteId) => apiClient.patch(`/api/v1/discrepancy-notes/${noteId}/resolve`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["discrepancy-notes"] });
    },
  });
}
