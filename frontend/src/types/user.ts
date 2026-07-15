export type StudyRole =
  | "admin"
  | "coordinator"
  | "investigator"
  | "dataManager"
  | "dataEntry"
  | "monitor"
  | "studyDirector"
  | "Study_Director"
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
  | "study:build"
  | "site:view"
  | "site:create"
  | "subject:view"
  | "subject:create"
  | "subject:edit"
  | "crf:design"
  | "crf:enter"
  | "crf:review"
  | "data:export"
  | "task:view"
  | "task:manage"
  | "participant:access"
  | "recruit:view"
  | "recruit:manage"
  | "analytics:view"
  | "sdv:view"
  | "sdv:review"
  | "fhir:view"
  | "fhir:manage"
  | "user:manage"
  | "randomization:view"
  | "randomization:configure"
  | "randomization:activate"
  | "randomization:assign"
  | "randomization:view_unblinded"
  | "randomization:unblind"
  | "randomization:export"
  | "audit:view"
  | "admin:access";

export const ROLE_PERMISSIONS: Record<StudyRole, Permission[]> = {
  "Study_Director": [
    "study:view", "study:edit", "study:create", "study:build",
    "site:view", "site:create",
    "subject:view", "subject:create", "subject:edit",
    "crf:design", "crf:enter", "crf:review",
    "data:export", "task:view", "task:manage", "participant:access", "recruit:view", "recruit:manage", "analytics:view", "fhir:view", "fhir:manage",
    "user:manage",
    "randomization:view", "randomization:configure", "randomization:activate",
    "randomization:assign", "randomization:view_unblinded", "randomization:unblind",
    "randomization:export",
    "audit:view", "sdv:view", "sdv:review",
    "admin:access",
  ],
  admin: [
    "study:view", "study:edit", "study:create", "study:build",
    "site:view", "site:create",
    "subject:view", "subject:create", "subject:edit",
    "crf:design", "crf:enter", "crf:review",
    "data:export", "task:view", "task:manage", "participant:access", "recruit:view", "recruit:manage", "analytics:view", "fhir:view", "fhir:manage",
    "user:manage",
    "randomization:view", "randomization:configure", "randomization:activate",
    "randomization:assign", "randomization:view_unblinded", "randomization:unblind",
    "randomization:export",
    "audit:view", "sdv:view", "sdv:review",
    "admin:access",
  ],
  coordinator: [
    "study:view", "study:edit", "study:build",
    "site:view", "site:create",
    "subject:view", "subject:create", "subject:edit",
    "crf:design", "crf:enter", "crf:review",
    "data:export", "task:view", "task:manage", "participant:access", "recruit:view", "recruit:manage", "analytics:view", "fhir:view", "fhir:manage",
    "randomization:view", "randomization:assign",
    "audit:view", "sdv:view", "sdv:review",
  ],
  studyDirector: [
    "study:view", "study:edit", "study:build",
    "site:view",
    "subject:view",
    "crf:design",
    "data:export", "task:view", "participant:access", "recruit:view", "analytics:view", "sdv:view", "fhir:view",
    "audit:view",
  ],
  investigator: [
    "study:view",
    "site:view",
    "subject:view", "subject:create",
    "crf:enter",
    "data:export", "task:view", "task:manage", "participant:access", "recruit:view", "recruit:manage", "analytics:view", "fhir:view",
  ],
  dataManager: [
    "study:view",
    "site:view",
    "subject:view", "subject:edit",
    "crf:design", "crf:enter", "crf:review",
    "data:export", "task:view", "task:manage", "analytics:view", "fhir:view", "fhir:manage",
    "audit:view", "analytics:view", "sdv:view", "sdv:review",
  ],
  dataEntry: [
    "study:view",
    "site:view",
    "subject:view",
    "crf:enter", "task:view",
  ],
  monitor: [
    "study:view",
    "site:view",
    "subject:view",
    "crf:review", "task:view",
    "audit:view", "sdv:view", "sdv:review",
  ],
  principalInvestigator: [
    "study:view",
    "site:view",
    "subject:view",
    "crf:review",
    "data:export", "task:view",
    "audit:view", "sdv:view", "sdv:review",
  ],
};
