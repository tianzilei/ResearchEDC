import { useMemo } from "react";
import { useAppMutation, useAppQuery, useQueryClient } from "@/hooks/useQuery";
import {
  taskApi,
  type CreateTaskRequest,
  type TaskFilters,
  type TaskInstanceDTO,
  type TaskStatus,
  type TaskTemplateDTO,
} from "@/api/tasks";

export type { CreateTaskRequest, TaskInstanceDTO, TaskStatus, TaskTemplateDTO };

export function useTasks(filters: TaskFilters) {
  const params = useMemo(() => filters, [filters.studyId, filters.status, filters.assignedToMe]);
  return useAppQuery<TaskInstanceDTO[]>({
    queryKey: ["tasks", params],
    queryFn: () => taskApi.listTasks(params),
    enabled: !!params.studyId || !!params.assignedToMe,
    staleTime: 30 * 1000,
  });
}

export function useCreateTask(studyId: number | undefined) {
  const qc = useQueryClient();
  return useAppMutation<TaskInstanceDTO, CreateTaskRequest>({
    mutationFn: (body) => taskApi.createTask(body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["tasks"] });
      if (studyId) qc.invalidateQueries({ queryKey: ["tasks", { studyId }] });
    },
  });
}

export function useCompleteTask() {
  const qc = useQueryClient();
  return useAppMutation<TaskInstanceDTO, number>({
    mutationFn: (id) => taskApi.completeTask(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tasks"] }),
  });
}

export function useCancelTask() {
  const qc = useQueryClient();
  return useAppMutation<TaskInstanceDTO, number>({
    mutationFn: (id) => taskApi.cancelTask(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["tasks"] }),
  });
}
