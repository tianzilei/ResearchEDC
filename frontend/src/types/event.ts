/**
 * Event types matching backend DTOs:
 * - StudyEventDTO
 * - EventDefinitionDTO
 * - EventCrfDTO
 * - ScheduleEventRequest
 */

export interface EventDefinitionDTO {
  studyEventDefinitionId: number;
  studyId: number;
  name: string;
  description: string;
  ordinal: number;
  category: string;
  statusId: number;
}

export interface CreateEventDefinitionRequest {
  studyId: number;
  name: string;
  description?: string;
  ordinal?: number;
  category?: string;
}

export interface StudyEventDTO {
  studyEventId: number;
  studySubjectId: number;
  studyEventDefinitionId: number;
  eventDefinitionName: string | null;
  location: string | null;
  dateStart: string | null;
  dateEnd: string | null;
  statusId: number;
  subjectEventStatusId: number;
  dateCreated: string;
  sedOrdinal: number | null;
}

export interface EventCrfDTO {
  eventCrfId: number;
  studyEventId: number;
  studySubjectId: number;
  crfVersionId: number;
  statusId: number;
  dateInterviewed: string | null;
  interviewerName: string;
  dateCompleted: string | null;
  dateValidate: string | null;
  electronicSignatureStatus: boolean | null;
  sdvStatus: boolean | null;
  dateCreated: string;
}

export interface ScheduleEventRequest {
  studySubjectId: number;
  studyEventDefinitionId: number;
  ordinal: number;
  location: string;
  startDate?: string;
  endDate?: string;
}
