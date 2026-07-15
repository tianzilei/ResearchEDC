import { useAppMutation, useAppQuery, useQueryClient } from "@/hooks/useQuery";
import {
  econsentApi,
  type AssignConsentRequest,
  type ConsentArtifactDTO,
  type ConsentAssignmentDTO,
  type ConsentAssignmentResultDTO,
  type ConsentTemplateDTO,
  type ConsentVersionDTO,
  type CountersignConsentRequest,
  type CreateConsentTemplateRequest,
  type CreateConsentVersionRequest,
} from "@/api/econsent";

export type {
  AssignConsentRequest,
  ConsentArtifactDTO,
  ConsentAssignmentDTO,
  ConsentAssignmentResultDTO,
  ConsentTemplateDTO,
  ConsentVersionDTO,
  CountersignConsentRequest,
  CreateConsentTemplateRequest,
  CreateConsentVersionRequest,
};

export function useConsentTemplates(studyId: number | undefined) {
  return useAppQuery<ConsentTemplateDTO[]>({
    queryKey: ["econsent", "templates", studyId],
    queryFn: () => (studyId ? econsentApi.listTemplates(studyId) : Promise.resolve([])),
    enabled: !!studyId,
    staleTime: 30 * 1000,
  });
}

export function useConsentVersions(templateId: number | undefined) {
  return useAppQuery<ConsentVersionDTO[]>({
    queryKey: ["econsent", "versions", templateId],
    queryFn: () => (templateId ? econsentApi.listVersions(templateId) : Promise.resolve([])),
    enabled: !!templateId,
    staleTime: 15 * 1000,
  });
}

export function useConsentAssignments(studyId: number | undefined) {
  return useAppQuery<ConsentAssignmentDTO[]>({
    queryKey: ["econsent", "assignments", studyId],
    queryFn: () => (studyId ? econsentApi.listAssignments(studyId) : Promise.resolve([])),
    enabled: !!studyId,
    staleTime: 15 * 1000,
  });
}

export function useCreateConsentTemplate(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<ConsentTemplateDTO, CreateConsentTemplateRequest>({
    mutationFn: (body) => econsentApi.createTemplate(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["econsent", "templates", studyId] }),
  });
}

export function useCreateConsentVersion(templateId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<ConsentVersionDTO, CreateConsentVersionRequest>({
    mutationFn: (body) => {
      if (!templateId) throw new Error("Template is required");
      return econsentApi.createVersion(templateId, body);
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ["econsent", "versions", templateId] }),
  });
}

export function usePublishConsentVersion(templateId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<ConsentVersionDTO, number>({
    mutationFn: (versionId) => econsentApi.publishVersion(versionId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["econsent", "versions", templateId] }),
  });
}

export function useAssignConsent(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<ConsentAssignmentResultDTO, AssignConsentRequest>({
    mutationFn: (body) => econsentApi.assignConsent(body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["econsent", "assignments", studyId] });
      qc.invalidateQueries({ queryKey: ["participant-access"] });
      qc.invalidateQueries({ queryKey: ["tasks"] });
    },
  });
}

export function useCountersignConsent(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<ConsentAssignmentDTO, { assignmentId: number; request: CountersignConsentRequest }>({
    mutationFn: ({ assignmentId, request }) => econsentApi.countersign(assignmentId, request),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["econsent", "assignments", studyId] });
      qc.invalidateQueries({ queryKey: ["tasks"] });
    },
  });
}
