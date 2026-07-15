import { apiClient } from "@/api/client";
import type { StudyDetail } from "@/types/study";

export interface StudyTemplateDTO {
  id: number;
  name: string;
  description: string | null;
  category: string | null;
  protocolType: string | null;
  phase: string | null;
  active: boolean;
  createdBy: number | null;
  createdDate: string;
  defaults: Record<string, unknown>;
}

export interface CreateStudyTemplateRequest {
  name: string;
  description?: string;
  category?: string;
  protocolType?: string;
  phase?: string;
  defaults?: Record<string, unknown>;
}

export interface ApplyStudyTemplateRequest {
  name: string;
  uniqueIdentifier: string;
  principalInvestigator?: string;
  facilityName?: string;
  sponsor?: string;
  expectedTotalEnrollment?: number;
}

export interface StudyTemplateApplicationDTO {
  template: StudyTemplateDTO;
  study: StudyDetail;
}

export const studyBuildApi = {
  listTemplates() {
    return apiClient.get<StudyTemplateDTO[]>("/api/v1/study-build/templates");
  },

  createTemplate(request: CreateStudyTemplateRequest) {
    return apiClient.post<StudyTemplateDTO>("/api/v1/study-build/templates", request);
  },

  applyTemplate(templateId: number, request: ApplyStudyTemplateRequest) {
    return apiClient.post<StudyTemplateApplicationDTO>(`/api/v1/study-build/templates/${templateId}/apply`, request);
  },
};
