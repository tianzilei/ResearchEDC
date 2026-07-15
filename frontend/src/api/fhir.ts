import { apiClient } from "@/api/client";

export type FhirImportStatus = "RECEIVED" | "MAPPED" | "RECONCILED" | "REJECTED" | "FAILED";

export interface FhirConnectorDTO {
  id: number;
  studyId: number;
  name: string;
  baseUrl: string;
  active: boolean;
  createdBy: number | null;
  createdDate: string;
}

export interface FhirImportRecordDTO {
  id: number;
  connectorId: number;
  studyId: number;
  resourceType: string;
  externalId: string;
  mappedSubjectIdentifier: string;
  mappedGender: string;
  status: FhirImportStatus;
  reviewNotes: string | null;
  createdBy: number | null;
  createdDate: string;
  updatedDate: string | null;
}

export interface CreateFhirConnectorRequest {
  studyId: number;
  name: string;
  baseUrl?: string;
}

export interface SubmitFhirResourceRequest {
  connectorId: number;
  payloadJson: string;
}

export interface ReconcileFhirRecordRequest {
  status: FhirImportStatus;
  reviewNotes?: string;
}

export const fhirApi = {
  listConnectors(studyId: number) {
    return apiClient.get<FhirConnectorDTO[]>("/api/v1/fhir/connectors", { studyId });
  },

  createConnector(request: CreateFhirConnectorRequest) {
    return apiClient.post<FhirConnectorDTO>("/api/v1/fhir/connectors", request);
  },

  listRecords(studyId: number, status?: FhirImportStatus) {
    return apiClient.get<FhirImportRecordDTO[]>("/api/v1/fhir/records", status ? { studyId, status } : { studyId });
  },

  submitResource(request: SubmitFhirResourceRequest) {
    return apiClient.post<FhirImportRecordDTO>("/api/v1/fhir/records", request);
  },

  reconcile(recordId: number, request: ReconcileFhirRecordRequest) {
    return apiClient.post<FhirImportRecordDTO>(`/api/v1/fhir/records/${recordId}/reconcile`, request);
  },
};
