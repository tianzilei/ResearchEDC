import { useAppQuery } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import type { RuleSetDTO } from "@/types/rules";

export function useRuleSets(studyId: number | undefined) {
  return useAppQuery<RuleSetDTO[]>({
    queryKey: ["rule-sets", studyId],
    queryFn: () =>
      studyId
        ? apiClient.get<RuleSetDTO[]>("/api/v1/rules/rule-sets", { studyId })
        : Promise.resolve([]),
    enabled: !!studyId,
  });
}

export function useRuleSet(ruleSetId: number | undefined) {
  return useAppQuery<RuleSetDTO>({
    queryKey: ["rule-set", ruleSetId],
    queryFn: () =>
      ruleSetId
        ? apiClient.get<RuleSetDTO>(`/api/v1/rules/rule-sets/${ruleSetId}`)
        : Promise.reject(new Error("ruleSetId is required")),
    enabled: !!ruleSetId,
  });
}

interface RuleDetailDTO {
  ruleId: number;
  name: string;
  description: string;
  expressionValue: string;
  enabled: boolean;
}

export function useRule(ruleId: number) {
  return useAppQuery<RuleDetailDTO>({
    queryKey: ["rule", ruleId],
    queryFn: () => apiClient.get<RuleDetailDTO>(`/api/v1/rules/rules/${ruleId}`),
    enabled: !!ruleId,
  });
}
