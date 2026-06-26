import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { studyApi, type FeatureFlags } from "@/api/studies";
import type { Study } from "@/types/study";

const DEFAULT_FLAGS: FeatureFlags = {};

export function useFeatureFlags(study: Study | null) {
  const studyId = study?.id;

  return useAppQuery<FeatureFlags>({
    queryKey: ["feature-flags", studyId],
    queryFn: async () => {
      if (!studyId) return DEFAULT_FLAGS;
      const data = await studyApi.getFeatureFlags(studyId);
      return data.flags ?? DEFAULT_FLAGS;
    },
    enabled: !!studyId,
    staleTime: 30_000,
  });
}

export function useUpdateFeatureFlags(studyId: number) {
  const queryClient = useQueryClient();

  return useAppMutation<void, FeatureFlags>({
    mutationFn: (flags) => studyApi.updateFeatureFlags(studyId, flags),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["feature-flags", studyId] });
    },
  });
}

export function isFlagEnabled(flags: FeatureFlags | undefined, flagName: string): boolean {
  if (!flags) return false;
  return flags[flagName] === true;
}
