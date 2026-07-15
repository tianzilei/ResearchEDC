import { useMutation, useQueryClient } from "@tanstack/react-query";
import { studyBuildApi, type ApplyStudyTemplateRequest, type CreateStudyTemplateRequest, type StudyTemplateDTO } from "@/api/studyBuild";
import { useAppQuery } from "@/hooks/useQuery";

export type { StudyTemplateDTO };

export function useStudyTemplates() {
  return useAppQuery<StudyTemplateDTO[]>({
    queryKey: ["study-build", "templates"],
    queryFn: () => studyBuildApi.listTemplates(),
  });
}

export function useCreateStudyTemplate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateStudyTemplateRequest) => studyBuildApi.createTemplate(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["study-build", "templates"] });
    },
  });
}

export function useApplyStudyTemplate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ templateId, request }: { templateId: number; request: ApplyStudyTemplateRequest }) =>
      studyBuildApi.applyTemplate(templateId, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["studies"] });
    },
  });
}
