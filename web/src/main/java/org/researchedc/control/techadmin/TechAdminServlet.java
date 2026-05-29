/*
 *
 */
package org.researchedc.control.techadmin;

//
// import java.util.ArrayList;

import org.researchedc.control.core.SecureController;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author thickerson
 *
 *
 */
public class TechAdminServlet extends SecureController {

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
        // find last 5 modifed studies
        IStudyDAO sdao = this.studyDao;
        // ArrayList studies = (ArrayList) sdao.findAllByLimit(true);
        // request.setAttribute("studies", studies);
        ArrayList allStudies = (ArrayList) sdao.findAll();
        // request.setAttribute("allStudyNumber", new
        // Integer(allStudies.size()));

        IUserAccountDAO udao = this.userAccountDao;
        // ArrayList users = (ArrayList) udao.findAllByLimit(true);
        // request.setAttribute("users", users);
        ArrayList allUsers = (ArrayList) udao.findAll();
        // request.setAttribute("allUserNumber", new Integer(allUsers.size()));

        ISubjectDAO subdao = this.subjectDao;
        // ArrayList subjects = (ArrayList) subdao.findAllByLimit(true);
        // request.setAttribute("subjects", subjects);
        ArrayList allSubjects = (ArrayList) subdao.findAll();
        // request.setAttribute("allSubjectNumber", new
        // Integer(allSubjects.size()));

        ICrfDAO cdao = this.crfDao;
        // ArrayList crfs = (ArrayList) cdao.findAllByLimit(true);
        // request.setAttribute("crfs", subjects);
        ArrayList allCrfs = (ArrayList) cdao.findAll();
        // request.setAttribute("allCrfNumber", new Integer(allCrfs.size()));

        resetPanel();

        panel.setStudyInfoShown(false);
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
        forwardPage(Page.TECH_ADMIN_SYSTEM);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        if (!ub.isTechAdmin()) {
            throw new InsufficientPermissionException(Page.MENU, resexception.getString("you_may_not_perform_technical_admin_functions"), "1");
        }

        return;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}
