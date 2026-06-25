import { apiClient } from "@/api/client";

export interface UserDTO {
  userId: number;
  userName: string;
  firstName: string | null;
  lastName: string | null;
  phone: string | null;
  institutionalAffiliation?: string | null;
  userType?: string | null;
  enabled: boolean;
  activeStudyId: number | null;
  dateCreated: string | null;
}

export interface RoleDTO {
  studyUserRoleId: number;
  roleName: string;
  userName: string;
  studyId: number | null;
  statusId: number | null;
}

export interface CreateUserRequest {
  userName: string;
  firstName?: string;
  lastName?: string;
  phone?: string | null;
  institutionalAffiliation?: string | null;
  userType?: string;
  statusId?: number;
}

export interface AssignRoleRequest {
  userName: string;
  studyId: number;
  roleName: string;
  statusId?: number;
}

export const identityApi = {
  listUsers(query = "") {
    return apiClient.get<UserDTO[]>("/api/v1/identity/users", { query });
  },

  createUser(request: CreateUserRequest) {
    return apiClient.post<UserDTO>("/api/v1/identity/users", request);
  },

  listRolesByUser(userName: string) {
    return apiClient.get<RoleDTO[]>("/api/v1/identity/roles/by-user", { userName });
  },

  listRolesByStudy(studyId: number) {
    return apiClient.get<RoleDTO[]>("/api/v1/identity/roles/by-study", { studyId });
  },

  assignRole(request: AssignRoleRequest) {
    return apiClient.post<void>("/api/v1/identity/roles/assign", request);
  },
};
