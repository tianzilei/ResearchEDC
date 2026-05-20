import { useAppQuery, useAppMutation } from "@/hooks/useQuery";
import { useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/api/client";
import type { StudyEventDTO, EventDefinitionDTO, EventCrfDTO, ScheduleEventRequest } from "@/types/event";

export function useEventDefinitions(studyId: number | undefined) {
  return useAppQuery<EventDefinitionDTO[]>({
    queryKey: ["event-definitions", studyId],
    queryFn: () =>
      studyId
        ? apiClient.get<EventDefinitionDTO[]>("/api/v1/events/definitions", { studyId })
        : Promise.resolve([]),
    enabled: !!studyId,
  });
}

export function useSubjectEvents(studySubjectId: number | undefined) {
  return useAppQuery<StudyEventDTO[]>({
    queryKey: ["subject-events", studySubjectId],
    queryFn: () =>
      studySubjectId
        ? apiClient.get<StudyEventDTO[]>("/api/v1/events/by-subject", { studySubjectId })
        : Promise.resolve([]),
    enabled: !!studySubjectId,
  });
}

export function useEventCrfs(eventId: number | undefined) {
  return useAppQuery<EventCrfDTO[]>({
    queryKey: ["event-crfs", eventId],
    queryFn: () =>
      eventId
        ? apiClient.get<EventCrfDTO[]>(`/api/v1/events/${eventId}/crfs`)
        : Promise.resolve([]),
    enabled: !!eventId,
  });
}

export function useScheduleEvent() {
  const queryClient = useQueryClient();

  return useAppMutation<void, ScheduleEventRequest>({
    mutationFn: (data) => apiClient.post("/api/v1/events", data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["subject-events"] });
    },
  });
}

export function useCompleteEvent() {
  const queryClient = useQueryClient();

  return useAppMutation<void, number>({
    mutationFn: (eventId) => apiClient.post(`/api/v1/events/${eventId}/complete`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["subject-events"] });
    },
  });
}
