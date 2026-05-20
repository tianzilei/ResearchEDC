export interface DiscrepancyNoteDTO {
  discrepancyNoteId: number;
  description: string;
  detailedNotes: string;
  type: string | null;
  resolutionStatus: string | null;
  entityType: string;
  column: string;
  entityId: number;
  studyId: number;
  ownerId: number;
  ownerName: string;
  dateCreated: string;
  parentDnId: number;
  hasChildren: boolean;
  eventCRFId: number;
  subjectName: string;
  eventName: string;
  crfName: string;
  entityName: string;
}

export interface CreateDiscrepancyNoteRequest {
  description: string;
  detailedNotes: string;
  entityType: string;
  entityId: number;
  studyId: number;
  eventCrfId: number;
  itemDataId: number;
}
