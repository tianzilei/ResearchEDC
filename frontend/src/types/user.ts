export type StudyRole =
  | "admin"
  | "coordinator"
  | "investigator"
  | "dataManager"
  | "dataEntry"
  | "monitor"
  | "studyDirector"
  | "principalInvestigator";

export interface UserStudyRole {
  studyId: number;
  studyName: string;
  studyOid: string;
  role: StudyRole;
  studyType: "study" | "site";
  parentStudyId?: number;
}

export type Permission =
  | "study:view"
  | "study:edit"
  | "study:create"
  | "site:view"
  | "site:create"
  | "subject:view"
  | "subject:create"
  | "subject:edit"
  | "crf:design"
  | "crf:enter"
  | "crf:review"
  | "data:export"
  | "user:manage"
  | "randomization:view"
  | "randomization:assign"
  | "audit:view"
  | "admin:access";

export const ROLE_PERMISSIONS: Record<StudyRole, Permission[]> = {
  admin: [
    "study:view", "study:edit", "study:create",
    "site:view", "site:create",
    "subject:view", "subject:create", "subject:edit",
    "crf:design", "crf:enter", "crf:review",
    "data:export",
    "user:manage",
    "randomization:view", "randomization:assign",
    "audit:view",
    "admin:access",
  ],
  coordinator: [
    "study:view", "study:edit",
    "site:view", "site:create",
    "subject:view", "subject:create", "subject:edit",
    "crf:design", "crf:enter", "crf:review",
    "data:export",
    "randomization:view",
    "audit:view",
  ],
  studyDirector: [
    "study:view", "study:edit",
    "site:view",
    "subject:view",
    "crf:design",
    "data:export",
    "audit:view",
  ],
  investigator: [
    "study:view",
    "site:view",
    "subject:view", "subject:create",
    "crf:enter",
    "data:export",
  ],
  dataManager: [
    "study:view",
    "site:view",
    "subject:view", "subject:edit",
    "crf:design", "crf:enter", "crf:review",
    "data:export",
    "audit:view",
  ],
  dataEntry: [
    "study:view",
    "site:view",
    "subject:view",
    "crf:enter",
  ],
  monitor: [
    "study:view",
    "site:view",
    "subject:view",
    "crf:review",
    "audit:view",
  ],
  principalInvestigator: [
    "study:view",
    "site:view",
    "subject:view",
    "crf:review",
    "data:export",
    "audit:view",
  ],
};
