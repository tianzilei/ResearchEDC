import { useAppQuery, useAppMutation } from "@/hooks/useQuery";
import { useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/api/client";
import type { SubjectGroupClassDTO, SubjectGroupDTO } from "@/types/subjectGroup";

export function useGroupClasses(studyId: number | undefined) {
  return useAppQuery<SubjectGroupClassDTO[]>({
    queryKey: ["group-classes", studyId],
    queryFn: () =>
      studyId
        ? apiClient.get<SubjectGroupClassDTO[]>("/api/legacy/subject-groups/classes", { studyId })
        : Promise.resolve([]),
    enabled: !!studyId,
  });
}

export function useGroupClass(classId: number | undefined) {
  return useAppQuery<SubjectGroupClassDTO>({
    queryKey: ["group-class", classId],
    queryFn: () =>
      classId
        ? apiClient.get<SubjectGroupClassDTO>(`/api/legacy/subject-groups/classes/${classId}`)
        : Promise.reject(new Error("classId is required")),
    enabled: !!classId,
  });
}

export function useClassGroups(classId: number | undefined) {
  return useAppQuery<SubjectGroupDTO[]>({
    queryKey: ["class-groups", classId],
    queryFn: () =>
      classId
        ? apiClient.get<SubjectGroupDTO[]>(`/api/legacy/subject-groups/classes/${classId}/groups`)
        : Promise.resolve([]),
    enabled: !!classId,
  });
}

export function useCreateGroupClass() {
  const queryClient = useQueryClient();
  return useAppMutation<SubjectGroupClassDTO, Partial<SubjectGroupClassDTO>>({
    mutationFn: (data) => apiClient.post("/api/legacy/subject-groups/classes", data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["group-classes"] }),
  });
}

export function useCreateGroup() {
  const queryClient = useQueryClient();
  return useAppMutation<SubjectGroupDTO, { classId: number; data: Partial<SubjectGroupDTO> }>({
    mutationFn: ({ classId, data }) =>
      apiClient.post(`/api/legacy/subject-groups/classes/${classId}/groups`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["group-classes"] });
      queryClient.invalidateQueries({ queryKey: ["class-groups"] });
    },
  });
}
