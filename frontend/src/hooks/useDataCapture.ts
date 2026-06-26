import { useAppMutation } from "@/hooks/useQuery";
import { useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/api/client";
import type { BatchSaveItemsRequest, SaveItemDataRequest } from "@/types/datacapture";

export function useSaveItemData(eventCrfId: number | undefined) {
  const queryClient = useQueryClient();

  return useAppMutation<void, SaveItemDataRequest>({
    mutationFn: (data) => apiClient.post("/api/v1/data-capture/items", data),
    onSuccess: () => {
      if (eventCrfId) {
        queryClient.invalidateQueries({ queryKey: ["event-crf-data", eventCrfId] });
      }
    },
  });
}

export function useBatchSaveItems(eventCrfId: number | undefined) {
  const queryClient = useQueryClient();

  return useAppMutation<void, BatchSaveItemsRequest>({
    mutationFn: (data) => apiClient.post("/api/v1/data-capture/items/batch", data),
    onSuccess: () => {
      if (eventCrfId) {
        queryClient.invalidateQueries({ queryKey: ["event-crf-data", eventCrfId] });
      }
    },
  });
}
