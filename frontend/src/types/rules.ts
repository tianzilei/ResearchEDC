export interface RuleSetDTO {
  ruleSetId: number;
  name: string;
  description: string;
  studyName: string;
  studyId: number;
  crfName: string;
  crfVersionName: string;
  eventDefinitionName: string;
  target: string;
  ownerId: number;
  dateCreated: string;
  ruleNames: string[];
}
