export interface Study {
  id: number;
  name: string;
  identifier: string;
  oid: string;
  type: "study" | "site";
  parentStudyId?: number;
  status: string;
  principalInvestigator?: string;
  facilityName?: string;
}

export interface StudySummary {
  study: Study;
  sites: Study[];
  role: string;
}

export interface StudyDetail {
  studyId: number;
  parentStudyId: number | null;
  site: boolean;
  name: string;
  uniqueIdentifier: string | null;
  secondaryIdentifier: string | null;
  ocOid: string | null;
  officialTitle: string | null;
  summary: string | null;
  phase: string | null;
  principalInvestigator: string | null;
  sponsor: string | null;
  collaborators: string | null;
  status: string;
  typeId: number | null;
  facilityName: string | null;
  facilityCity: string | null;
  facilityState: string | null;
  facilityCountry: string | null;
  datePlannedStart: string | null;
  datePlannedEnd: string | null;
  dateCreated: string | null;
  dateUpdated: string | null;
  ownerId: number | null;
  expectedTotalEnrollment: number | null;
  protocolType: string | null;
  protocolDescription: string | null;
  conditions: string | null;
  keywords: string | null;
  eligibility: string | null;
  gender: string | null;
  purpose: string | null;
  allocation: string | null;
  masking: string | null;
  sites: StudySummaryItem[];
}

export interface StudySummaryItem {
  studyId: number;
  name: string;
  uniqueIdentifier: string;
  status: string;
  principalInvestigator: string;
  site: boolean;
  facilityCity?: string;
  facilityState?: string;
  facilityCountry?: string;
}
