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
  status: string;
}

export interface StudyEventDTO {
  id: number;
  studyEventDefinitionId: number;
  studySubjectId: number;
  label: string;
  ordinal: number;
  dateStarted: string;
  dateEnded: string | null;
  statusId: number;
  location: string;
  dateCreated: string;
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
}
