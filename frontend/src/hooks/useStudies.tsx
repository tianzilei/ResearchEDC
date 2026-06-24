import {
  createContext,
  useContext,
  useState,
  useCallback,
  type ReactNode,
} from "react";
import { useAppQuery, useQueryClient } from "@/hooks/useQuery";
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

// eslint-disable-next-line react-refresh/only-export-components
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

// ── Reactive Study Context ────────────────────────────────────────────

const STORE_KEY = "oc_current_study";

interface StudyContextValue {
  currentStudy: Study | null;
  setCurrentStudy: (study: Study) => void;
  clearCurrentStudy: () => void;
}

const StudyContext = createContext<StudyContextValue | null>(null);

function getStoredStudy(): Study | null {
  try {
    const raw = sessionStorage.getItem(STORE_KEY);
    return raw ? (JSON.parse(raw) as Study) : null;
  } catch {
    sessionStorage.removeItem(STORE_KEY);
    return null;
  }
}

export function StudyProvider({ children }: { children: ReactNode }) {
  const [currentStudy, setState] = useState<Study | null>(getStoredStudy);
  const queryClient = useQueryClient();

  const setCurrentStudy = useCallback(
    (study: Study) => {
      sessionStorage.setItem(STORE_KEY, JSON.stringify(study));
      setState(study);
      // Invalidate all query caches that depend on the current study,
      // so TanStack Query refetches data for the newly-selected study.
      void queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      void queryClient.invalidateQueries({ queryKey: ["crfs"] });
      void queryClient.invalidateQueries({ queryKey: ["event-definitions"] });
      void queryClient.invalidateQueries({ queryKey: ["subject-events"] });
      void queryClient.invalidateQueries({ queryKey: ["group-classes"] });
      void queryClient.invalidateQueries({ queryKey: ["randomization"] });
    },
    [queryClient],
  );

  const clearCurrentStudy = useCallback(() => {
    sessionStorage.removeItem(STORE_KEY);
    setState(null);
  }, []);

  return (
    <StudyContext.Provider
      value={{ currentStudy, setCurrentStudy, clearCurrentStudy }}
    >
      {children}
    </StudyContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useCurrentStudy(): StudyContextValue {
  const ctx = useContext(StudyContext);
  if (!ctx) {
    throw new Error(
      "useCurrentStudy must be used within a <StudyProvider>",
    );
  }
  return ctx;
}
