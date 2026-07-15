import { analyticsApi, type AnalyticsDashboardDTO } from "@/api/analytics";
import { useAppQuery } from "@/hooks/useQuery";

export type { AnalyticsDashboardDTO };

export function useAnalyticsDashboard(studyId: number | undefined) {
  return useAppQuery<AnalyticsDashboardDTO>({
    queryKey: ["analytics", "dashboard", studyId],
    queryFn: () => analyticsApi.dashboard(studyId ?? 0),
    enabled: !!studyId,
    staleTime: 60 * 1000,
  });
}
