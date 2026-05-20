import { useAppQuery } from "@/hooks/useQuery";
import type { Study, StudySummary } from "@/types/study";

/**
 * Fetch studies and sites accessible to the current user.
 *
 * TODO: Replace placeholder endpoint with actual backend API once
 *       the studies listing endpoint is available for the React SPA.
 */

export function useStudies() {
  return useAppQuery<StudySummary[]>({
    queryKey: ["studies"],
    queryFn: async () => {
      const response = await fetch("/research-edc/auth/api/v1/studies");
      if (!response.ok) {
        throw new Error("Failed to fetch studies");
      }
      return response.json() as Promise<StudySummary[]>;
    },
    enabled: false,
  });
}

export function useCurrentStudy() {
  const storeKey = "oc_current_study";
  const stored = sessionStorage.getItem(storeKey);

  return {
    currentStudy: stored ? (JSON.parse(stored) as Study) : null,
    setCurrentStudy: (study: Study) => {
      sessionStorage.setItem(storeKey, JSON.stringify(study));
    },
    clearCurrentStudy: () => {
      sessionStorage.removeItem(storeKey);
    },
  };
}
