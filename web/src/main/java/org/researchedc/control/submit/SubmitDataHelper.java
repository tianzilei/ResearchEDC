package org.researchedc.control.submit;

import org.researchedc.bean.core.Role;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;

public final class SubmitDataHelper {

    private SubmitDataHelper() {
    }

    public static boolean mayViewData(UserAccountBean ub, StudyUserRoleBean currentRole) {
        if (currentRole != null) {
            Role r = currentRole.getRole();
            if (r != null && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) ||
                    r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r.equals(Role.RESEARCHASSISTANT2) || r.equals(Role.MONITOR))) {
                return true;
            }
        }
        return false;
    }

    public static boolean maySubmitData(UserAccountBean ub, StudyUserRoleBean currentRole) {
        if (currentRole != null) {
            Role r = currentRole.getRole();
            if (r != null && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) ||
                    r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r.equals(Role.RESEARCHASSISTANT2))) {
                return true;
            }
        }
        return false;
    }
}
