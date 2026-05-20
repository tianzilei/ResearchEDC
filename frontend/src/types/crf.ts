/**
 * CRF (Case Report Form) types matching backend DTOs:
 * - CrfSummaryDTO
 * - CrfVersionDTO
 * - SectionDTO
 * - ItemDTO
 */

export interface CrfSummary {
  crfId: number;
  name: string;
  description: string;
  ocOid: string;
  status: string;
  versionCount: number;
  dateCreated: string;
  dateUpdated: string;
}

export interface CrfVersion {
  crfVersionId: number;
  crfId: number;
  name: string;
  description: string;
  revisionNotes: string;
  ocOid: string;
  status: string;
  dateCreated: string;
  sections: SectionDTO[];
}

export interface SectionDTO {
  sectionId: number;
  crfVersionId: number;
  label: string;
  title: string;
  ordinal: number;
}

export interface ItemDTO {
  itemId: number;
  name: string;
  description: string;
  units: string;
  dataType: string;
  ocOid: string;
  responseType: string;
  phi: boolean;
  ordinal: number;
  defaultValue: string;
  required: boolean;
  regexp: string;
  regexpErrorMsg: string;
}
