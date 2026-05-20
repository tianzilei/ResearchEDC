import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import type { Study } from "@/types/study";

interface FeatureFlags {
  [key: string]: boolean;
}

interface FeatureFlagsResponse {
  flags: FeatureFlags;
}

const DEFAULT_FLAGS: FeatureFlags = {};

export function useFeatureFlags(study: Study | null) {
  const studyId = study?.id;

  return useAppQuery<FeatureFlags>({
    queryKey: ["feature-flags", studyId],
    queryFn: async () => {
      if (!studyId) return DEFAULT_FLAGS;
      const response = await fetch(`/api/v1/studies/${studyId}/feature-flags`);
      if (!response.ok) return DEFAULT_FLAGS;
      const data: FeatureFlagsResponse = await response.json();
      return data.flags ?? DEFAULT_FLAGS;
    },
    enabled: !!studyId,
    staleTime: 30_000,
  });
}

export function useUpdateFeatureFlags(studyId: number) {
  const queryClient = useQueryClient();

  return useAppMutation<void, FeatureFlags>({
    mutationFn: async (flags) => {
      const response = await fetch(`/api/v1/studies/${studyId}/feature-flags`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ flags }),
      });
      if (!response.ok) throw new Error("Failed to update feature flags");
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["feature-flags", studyId] });
    },
  });
}

export function isFlagEnabled(flags: FeatureFlags | undefined, flagName: string): boolean {
  if (!flags) return false;
  return flags[flagName] === true;
}
