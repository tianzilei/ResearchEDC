/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.bean.core.Role;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.Locale;

/**
 * @author ssachs
 */
public class ListStudySubjectsManageServlet extends ListStudySubjectServlet {

    Locale locale;

    // < ResourceBundleresexception,respage;

    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // <
        // resexception=ResourceBundle.getBundle("org.researchedc.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",locale);

        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.MONITOR) ||
          currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MANAGE_STUDY_SERVLET, resexception.getString("not_study_director"), "1");

    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.managestudy.ListStudySubjectServlet#getJSP()
     */
    @Override
    protected Page getJSP() {
        return Page.LIST_STUDY_SUBJECT;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.managestudy.ListStudySubjectServlet#getBaseURL()
     */
    @Override
    protected String getBaseURL() {
        return "ListStudySubjects";
    }
}
