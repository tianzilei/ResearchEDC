export type RandomizationAlgorithm = "SIMPLE" | "BLOCK" | "STRATIFIED_BLOCK";
export type SchemeStatus = "DRAFT" | "ACTIVE" | "PAUSED" | "CLOSED";
export type StratumType = "ENUM" | "INTERVAL";
export type AssignmentStatus = "ACTIVE" | "UNBLINDED" | "REVOKED";
export type UnblindingStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface ArmDTO {
  id?: number;
  name: string;
  displayName?: string;
  ratio: number;
  orderNumber: number;
}

export interface StratumOptionDTO {
  id?: number;
  label: string;
  value: string;
  orderNumber?: number;
}

export interface StratumDTO {
  id?: number;
  name: string;
  stratumType: StratumType;
  orderNumber: number;
  options: StratumOptionDTO[];
}

export interface SchemeDTO {
  id?: number;
  studyId: number;
  name: string;
  algorithm: RandomizationAlgorithm;
  status?: SchemeStatus;
  seed?: number;
  minBlockSize?: number;
  maxBlockSize?: number;
  arms: ArmDTO[];
  stratifications: StratumDTO[];
  totalAssigned?: number;
  totalArms?: number;
}

export interface SchemeSummaryDTO {
  id: number;
  studyId: number;
  name: string;
  algorithm: RandomizationAlgorithm;
  status: SchemeStatus;
  totalAssigned: number;
  totalArms: number;
}

export interface RandomizeRequest {
  schemeId: number;
  studySubjectId: number;
  assignedBy?: number;
  stratumValues?: Record<string, string>;
}

export interface AssignmentDTO {
  id: number;
  schemeId: number;
  studySubjectId: number;
  subjectKey?: string;
  armId: number;
  armName: string;
  stratumPath?: string;
  status: AssignmentStatus;
  assignedDate?: string;
  assignedBy?: number;
}

export interface UnblindingRequestDTO {
  id: number;
  assignmentId: number;
  armName?: string;
  subjectKey?: string;
  requestedBy: number;
  requestedDate?: string;
  reason?: string;
  status: UnblindingStatus;
  reviewedBy?: number;
  reviewedDate?: string;
  reviewNotes?: string;
}

export interface AuditLogDTO {
  id: number;
  schemeId?: number;
  studyId?: number;
  action: string;
  entityType?: string;
  entityId?: number;
  oldValue?: string;
  newValue?: string;
  performedBy?: number;
  performedDate: string;
  details?: string;
}
