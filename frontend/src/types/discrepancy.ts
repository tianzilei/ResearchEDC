export interface DiscrepancyNoteDTO {
  discrepancyNoteId: number;
  description: string;
  detailedNotes: string;
  entityType: string;
  entityId: number;
  studyId: number;
  ownerId: number;
  parentDnId: number;
  dateCreated: string;
  resolutionStatusId: number | null;
  discrepancyNoteTypeId: number | null;
}

export interface CreateDiscrepancyNoteRequest {
  description: string;
  detailedNotes: string;
  entityType: string;
  entityId: number;
  studyId: number;
  eventCrfId?: number;
  itemDataId?: number;
}
