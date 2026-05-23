import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import type { CrfSummary, CrfVersion, CrfVersionEntity, ItemDTO } from "@/types/crf";
import type { ItemDataDTO, ResponseSetDTO, ItemGroupDTO } from "@/types/datacapture";

export function useCrfList() {
  return useAppQuery<CrfSummary[]>({
    queryKey: ["crfs"],
    queryFn: () => apiClient.get<CrfSummary[]>("/api/v1/crfs"),
  });
}

export function useCrfVersion(crfVersionId: number | undefined) {
  return useAppQuery<CrfVersion | null>({
    queryKey: ["crf-version", crfVersionId],
    queryFn: () =>
      crfVersionId
        ? apiClient.get<CrfVersion>(`/api/v1/crfs/versions/${crfVersionId}`)
        : Promise.resolve(null),
    enabled: !!crfVersionId,
  });
}

export function useCrfSectionItems(crfVersionId: number | undefined, sectionId: number | undefined) {
  return useAppQuery<ItemDTO[]>({
    queryKey: ["crf-section-items", crfVersionId, sectionId],
    queryFn: () =>
      crfVersionId && sectionId
        ? apiClient.get<ItemDTO[]>(
            `/api/v1/crfs/versions/${crfVersionId}/sections/${sectionId}/items`,
          )
        : Promise.resolve([]),
    enabled: !!crfVersionId && !!sectionId,
  });
}

export function useEventCrfData(eventCrfId: number | undefined) {
  return useAppQuery<ItemDataDTO[]>({
    queryKey: ["event-crf-data", eventCrfId],
    queryFn: () =>
      eventCrfId
        ? apiClient.get<ItemDataDTO[]>("/api/v1/data-capture/items", {
            eventCrfId,
          })
        : Promise.resolve([]),
    enabled: !!eventCrfId,
  });
}

export function useResponseSet(responseSetId: number | undefined) {
  return useAppQuery<ResponseSetDTO>({
    queryKey: ["response-set", responseSetId],
    queryFn: () =>
      responseSetId
        ? apiClient.get<ResponseSetDTO>(`/api/v1/data-capture/response-sets/${responseSetId}`)
        : Promise.reject(new Error("responseSetId is required")),
    enabled: !!responseSetId,
  });
}

export function useItemGroups(crfId: number | undefined) {
  return useAppQuery<ItemGroupDTO[]>({
    queryKey: ["item-groups", crfId],
    queryFn: () =>
      crfId
        ? apiClient.get<ItemGroupDTO[]>("/api/v1/data-capture/item-groups", { crfId })
        : Promise.resolve([]),
    enabled: !!crfId,
  });
}

export function useCrfVersions(crfId: number | undefined) {
  return useAppQuery<CrfVersionEntity[]>({
    queryKey: ["crf-versions", crfId],
    queryFn: () =>
      crfId
        ? apiClient.get<CrfVersionEntity[]>(`/api/v1/crfs/${crfId}/versions`)
        : Promise.resolve([]),
    enabled: !!crfId,
  });
}

export function useUpdateCrfVersionStatus() {
  const qc = useQueryClient();
  return useAppMutation<void, { crfVersionId: number; statusId: number }>({
    mutationFn: ({ crfVersionId, statusId }) =>
      apiClient.patch(`/api/v1/crfs/versions/${crfVersionId}/status`, { statusId }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["crf-versions"] });
    },
  });
}

export function useDeleteCrfVersion() {
  const qc = useQueryClient();
  return useAppMutation<void, number>({
    mutationFn: (crfVersionId) =>
      apiClient.delete(`/api/v1/crfs/versions/${crfVersionId}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["crf-versions"] });
    },
  });
}

export function useCreateCrfVersion() {
  const qc = useQueryClient();
  return useAppMutation<CrfVersionEntity, { crfId: number; name: string; description: string }>({
    mutationFn: ({ crfId, ...body }) =>
      apiClient.post<CrfVersionEntity>(`/api/v1/crfs/${crfId}/versions`, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["crf-versions"] });
    },
  });
}
