import { useMemo } from "react";
import { useAuth } from "@/providers/AuthProvider";
import { ROLE_PERMISSIONS, type Permission, type StudyRole } from "@/types/user";

export function usePermissions(): Permission[] {
  const { user } = useAuth();

  return useMemo(() => {
    if (!user) return [];

    const allRoles = user.roles as StudyRole[];
    const permissionSet = new Set<Permission>();

    for (const role of allRoles) {
      const perms = ROLE_PERMISSIONS[role];
      if (perms) {
        for (const p of perms) {
          permissionSet.add(p);
        }
      }
    }

    return [...permissionSet];
  }, [user]);
}

export function useHasPermission(permission: Permission): boolean {
  const permissions = usePermissions();
  return permissions.includes(permission);
}
