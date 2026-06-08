/*
 * Minimal stub for EditStudyUserRoleServlet — retains only getLink() still referenced by UserAccountTable.
 * Full implementation deleted in Phase 1 study/subject/event slice.
 */
package org.researchedc.control.admin;

import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;

/**
 * Stub retaining the link-generation helper used by UserAccountTable.
 */
public class EditStudyUserRoleServlet {

    private static final String PATH = "EditStudyUserRole";
    private static final String ARG_STUDY_ID = "studyId";
    private static final String ARG_USER_NAME = "name";

    private EditStudyUserRoleServlet() {
        // Utility stub — not instantiable
    }

    /**
     * Generates the edit-role link for the given study user role and user.
     *
     * @param s    the study user role bean
     * @param user the user account bean
     * @return a relative URL string for the edit-role action
     */
    public static String getLink(StudyUserRoleBean s, UserAccountBean user) {
        int studyId = s.getStudyId();
        return PATH + "?" + ARG_STUDY_ID + "=" + studyId + "&" + ARG_USER_NAME + "=" + user.getName();
    }
}
