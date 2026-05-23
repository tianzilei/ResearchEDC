/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.bean.core.Role;
import org.researchedc.control.core.SecureController;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A super class of ChangeDefinitionOrdinal and Change DefinitionCRFOrdinal
 * Servlets
 *
 * @author jxu
 */
public abstract class ChangeOrdinalServlet extends SecureController {
    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

}
