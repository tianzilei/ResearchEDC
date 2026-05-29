import { useAppQuery } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import type { Study, StudySummary } from "@/types/study";

interface StudySummaryDTO {
  studyId: number;
  parentStudyId: number | null;
  site: boolean;
  name: string;
  uniqueIdentifier: string;
  ocOid: string;
  status: string;
  principalInvestigator?: string;
}

function mapDtoToStudy(dto: StudySummaryDTO): Study {
  return {
    id: dto.studyId,
    name: dto.name,
    identifier: dto.uniqueIdentifier ?? "",
    oid: dto.ocOid ?? "",
    type: dto.site ? "site" : "study",
    parentStudyId: dto.parentStudyId ?? undefined,
    status: dto.status ?? "available",
    principalInvestigator: dto.principalInvestigator,
  };
}

function groupStudies(dtos: StudySummaryDTO[]): StudySummary[] {
  const studies = dtos.filter((d) => !d.site && d.parentStudyId == null);
  const sites = dtos.filter((d) => d.site);
  return studies.map((s) => ({
    study: mapDtoToStudy(s),
    sites: sites
      .filter((site) => site.parentStudyId === s.studyId)
      .map(mapDtoToStudy),
    role: "",
  }));
}

export function useStudies() {
  return useAppQuery<StudySummary[]>({
    queryKey: ["studies"],
    queryFn: async () => {
      const dtos = await apiClient.get<StudySummaryDTO[]>("/api/v1/studies");
      return groupStudies(dtos);
    },
    staleTime: 5 * 60 * 1000,
  });
}

export function useCurrentStudy() {
  const storeKey = "oc_current_study";

  const getStored = (): Study | null => {
    try {
      const stored = sessionStorage.getItem(storeKey);
      return stored ? (JSON.parse(stored) as Study) : null;
    } catch {
      sessionStorage.removeItem(storeKey);
      return null;
    }
  };

  return {
    currentStudy: getStored(),
    setCurrentStudy: (study: Study) => {
      sessionStorage.setItem(storeKey, JSON.stringify(study));
    },
    clearCurrentStudy: () => {
      sessionStorage.removeItem(storeKey);
    },
  };
}
