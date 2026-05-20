import { useAppQuery } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import type { RuleSetDTO } from "@/types/rules";

export function useRuleSets(studyId: number | undefined) {
  return useAppQuery<RuleSetDTO[]>({
    queryKey: ["rule-sets", studyId],
    queryFn: () =>
      studyId
        ? apiClient.get<RuleSetDTO[]>("/api/legacy/rule-sets", { studyId })
        : Promise.resolve([]),
    enabled: !!studyId,
  });
}

export function useRuleSet(ruleSetId: number | undefined) {
  return useAppQuery<RuleSetDTO>({
    queryKey: ["rule-set", ruleSetId],
    queryFn: () =>
      ruleSetId
        ? apiClient.get<RuleSetDTO>(`/api/legacy/rule-sets/${ruleSetId}`)
        : Promise.reject(new Error("ruleSetId is required")),
    enabled: !!ruleSetId,
  });
}
