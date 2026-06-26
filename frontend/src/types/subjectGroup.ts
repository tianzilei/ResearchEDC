export interface SubjectGroupDTO {
  groupId: number;
  name: string;
  description: string;
  groupClassId: number;
  subjectCount: number;
}

export interface SubjectGroupClassDTO {
  groupClassId: number;
  name: string;
  studyId: number;
  groupClassType: string;
  subjectAssignment: string;
  groups: SubjectGroupDTO[];
  ownerId: number;
  dateCreated: string;
}
