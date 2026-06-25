import { apiClient } from "@/api/client";

export interface Dataset {
  datasetId: number;
  name: string;
  description: string;
  studyId: number;
  ownerId: number;
  statusId: number;
  dateCreated: string;
}

export interface FilterItem {
  filterId: number;
  name: string;
  description: string;
  ownerId: number;
  dateCreated: string;
}

export const datasetsApi = {
  list(studyId?: number) {
    const params = studyId ? { studyId } : undefined;
    return apiClient.get<Dataset[]>("/api/v1/datasets", params);
  },

  get(id: number) {
    return apiClient.get<Dataset>(`/api/v1/datasets/${id}`);
  },

  create(data: { name: string; studyId: number }) {
    return apiClient.post<Dataset>("/api/v1/datasets", data);
  },
};

export const filtersApi = {
  list() {
    return apiClient.get<FilterItem[]>("/api/v1/filters");
  },

  get(id: number) {
    return apiClient.get<FilterItem>(`/api/v1/filters/${id}`);
  },

  create(data: { name: string; description?: string }) {
    return apiClient.post<FilterItem>("/api/v1/filters", data);
  },
};
