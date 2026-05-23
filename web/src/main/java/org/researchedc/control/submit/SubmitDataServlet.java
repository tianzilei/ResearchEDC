/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.submit;

import org.researchedc.bean.core.Role;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ssachs
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SubmitDataServlet extends SecureController {

    Locale locale;

    // < ResourceBundleresexception,respage;

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
        forwardPage(Page.SUBMIT_DATA);
    }

    public static boolean mayViewData(UserAccountBean ub, StudyUserRoleBean currentRole) {
        if (currentRole != null) {
            Role r = currentRole.getRole();
            if (r != null && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) ||
                    r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r.equals(Role.RESEARCHASSISTANT2) ||r.equals(Role.MONITOR) )) {
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

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // <
        // resexception=ResourceBundle.getBundle("org.researchedc.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",locale);

        String exceptionName = resexception.getString("no_permission_to_submit_data");
        String noAccessMessage = respage.getString("may_not_enter_data_for_this_study") + respage.getString("change_study_contact_sysadmin");

        if (mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(noAccessMessage);
        throw new InsufficientPermissionException(Page.MENU, exceptionName, "1");
    }

}
