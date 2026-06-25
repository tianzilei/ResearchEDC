import { apiClient } from "@/api/client";
import type {
  CreateEventDefinitionRequest,
  EventCrfDTO,
  EventDefinitionDTO,
  ScheduleEventRequest,
  StudyEventDTO,
} from "@/types/event";

export const eventApi = {
  listDefinitions(studyId: number) {
    return apiClient.get<EventDefinitionDTO[]>("/api/v1/events/definitions", { studyId });
  },

  createDefinition(request: CreateEventDefinitionRequest) {
    return apiClient.post<EventDefinitionDTO>("/api/v1/events/definitions", request);
  },

  listSubjectEvents(studySubjectId: number) {
    return apiClient.get<StudyEventDTO[]>("/api/v1/events/by-subject", { studySubjectId });
  },

  listEventCrfs(eventId: number) {
    return apiClient.get<EventCrfDTO[]>(`/api/v1/events/${eventId}/crfs`);
  },

  scheduleEvent(request: ScheduleEventRequest) {
    return apiClient.post<void>("/api/v1/events", request);
  },

  completeEvent(eventId: number) {
    return apiClient.post<void>(`/api/v1/events/${eventId}/complete`);
  },
};
