/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.control.core.SecureController;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ssachs
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AdminSystemServlet extends SecureController {

    Locale locale;

    // < ResourceBundleresword,resexception;
    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {

        // find last 5 modifed studies
        IStudyDAO sdao = this.studyDao;
        ArrayList studies = (ArrayList) sdao.findAllByLimit(true);
        request.setAttribute("studies", studies);
        ArrayList allStudies = (ArrayList) sdao.findAll();
        request.setAttribute("allStudyNumber", Integer.valueOf(allStudies.size()));

        IUserAccountDAO udao = this.userAccountDao;
        ArrayList users = (ArrayList) udao.findAllByLimit(true);
        request.setAttribute("users", users);
        ArrayList allUsers = (ArrayList) udao.findAll();
        request.setAttribute("allUserNumber", Integer.valueOf(allUsers.size()));

        ISubjectDAO subdao = this.subjectDao;
        ArrayList subjects = (ArrayList) subdao.findAllByLimit(true);
        request.setAttribute("subjects", subjects);
        ArrayList allSubjects = (ArrayList) subdao.findAll();
        request.setAttribute("allSubjectNumber", Integer.valueOf(allSubjects.size()));

        ICrfDAO cdao = this.crfDao;
        ArrayList crfs = (ArrayList) cdao.findAllByLimit(true);
        request.setAttribute("crfs", crfs);
        ArrayList allCrfs = (ArrayList) cdao.findAll();
        request.setAttribute("allCrfNumber", Integer.valueOf(allCrfs.size()));

        resetPanel();
        panel.setOrderedData(true);
        setToPanel(resword.getString("in_the_application"), "");
        if (allSubjects.size() > 0) {
            setToPanel(resword.getString("subjects"), Integer.valueOf(allSubjects.size()).toString());
        }
        if (allUsers.size() > 0) {
            setToPanel(resword.getString("users"), Integer.valueOf(allUsers.size()).toString());
        }
        if (allStudies.size() > 0) {
            setToPanel(resword.getString("studies"), Integer.valueOf(allStudies.size()).toString());
        }
        if (allCrfs.size() > 0) {
            setToPanel(resword.getString("CRFs"), Integer.valueOf(allCrfs.size()).toString());
        }

        panel.setStudyInfoShown(false);
        forwardPage(Page.ADMIN_SYSTEM);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < resword =
        // ResourceBundle.getBundle("org.researchedc.i18n.words",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.researchedc.i18n.exceptions",locale);

        if (!ub.isSysAdmin()) {
            throw new InsufficientPermissionException(Page.MENU, "You may not perform administrative functions", "1");
        }

        return;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}
