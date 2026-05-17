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
