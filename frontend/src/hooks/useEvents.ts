import { useAppQuery, useAppMutation } from "@/hooks/useQuery";
import { useQueryClient } from "@tanstack/react-query";
import { eventApi } from "@/api/events";
import type { StudyEventDTO, EventDefinitionDTO, EventCrfDTO, ScheduleEventRequest } from "@/types/event";

export function useEventDefinitions(studyId: number | undefined) {
  return useAppQuery<EventDefinitionDTO[]>({
    queryKey: ["event-definitions", studyId],
    queryFn: () =>
      studyId
        ? eventApi.listDefinitions(studyId)
        : Promise.resolve([]),
    enabled: !!studyId,
  });
}

export function useSubjectEvents(studySubjectId: number | undefined) {
  return useAppQuery<StudyEventDTO[]>({
    queryKey: ["subject-events", studySubjectId],
    queryFn: () =>
      studySubjectId
        ? eventApi.listSubjectEvents(studySubjectId)
        : Promise.resolve([]),
    enabled: !!studySubjectId,
  });
}

export function useEventCrfs(eventId: number | undefined) {
  return useAppQuery<EventCrfDTO[]>({
    queryKey: ["event-crfs", eventId],
    queryFn: () =>
      eventId
        ? eventApi.listEventCrfs(eventId)
        : Promise.resolve([]),
    enabled: !!eventId,
  });
}

export function useScheduleEvent() {
  const queryClient = useQueryClient();

  return useAppMutation<void, ScheduleEventRequest>({
    mutationFn: (data) => eventApi.scheduleEvent(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["subject-events"] });
    },
  });
}

export function useCompleteEvent() {
  const queryClient = useQueryClient();

  return useAppMutation<void, number>({
    mutationFn: (eventId) => eventApi.completeEvent(eventId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["subject-events"] });
    },
  });
}
