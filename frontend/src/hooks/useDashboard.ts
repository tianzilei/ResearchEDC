import { useAppQuery } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";

export interface UserInfo {
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

export interface StudyInfo {
  studyId: number;
  name: string;
  site: boolean;
  parentStudyId: number | null;
  role: string;
}

export interface ModuleInfo {
  key: string;
  name: string;
  description: string;
  path: string;
  priority: number;
}

export interface BootstrapResponse {
  user: UserInfo;
  studies: StudyInfo[];
  defaultStudy: StudyInfo | null;
  modules: ModuleInfo[];
}

export interface TasksResponse {
  pendingCrfs: number;
  pendingQueries: number;
  pendingReviews: number;
  pendingAccountModifications: number;
}

export interface StatusResponse {
  database: string;
  backgroundTasks: string;
  lastBackup: string | null;
}

export interface RecentActivityItem {
  type: string;
  description: string;
  timestamp: string;
  link: string | null;
}

export function useBootstrap() {
  return useAppQuery<BootstrapResponse>({
    queryKey: ["dashboard", "bootstrap"],
    queryFn: () => apiClient.get<BootstrapResponse>("/api/v1/dashboard/bootstrap"),
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });
}

export function useDashboardTasks() {
  return useAppQuery<TasksResponse>({
    queryKey: ["dashboard", "tasks"],
    queryFn: () => apiClient.get<TasksResponse>("/api/v1/dashboard/tasks"),
    staleTime: 60 * 1000,
    retry: 1,
  });
}

export function useDashboardStatus() {
  return useAppQuery<StatusResponse>({
    queryKey: ["dashboard", "status"],
    queryFn: () => apiClient.get<StatusResponse>("/api/v1/dashboard/status"),
    staleTime: 30 * 1000,
    retry: 1,
  });
}

export function useDashboardRecent() {
  return useAppQuery<RecentActivityItem[]>({
    queryKey: ["dashboard", "recent"],
    queryFn: () => apiClient.get<RecentActivityItem[]>("/api/v1/dashboard/recent"),
    staleTime: 60 * 1000,
    retry: 1,
  });
}
