/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.control.core.SecureController;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;

/**
 * Processes user request and generate subject list
 *
 * @author jxu
 */
public class ListSubjectServlet extends SecureController {
    @Autowired
    private ISubjectDAO subjectDao;
    @Autowired
    private IUserAccountDAO userAccountDao;
    Locale locale;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);

        if (ub.isSysAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.ADMIN_SYSTEM_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        ISubjectDAO sdao = this.subjectDao;

        IStudySubjectDAO subdao = this.studySubjectDao;
        IStudyDAO studyDao = this.studyDao;
        IUserAccountDAO uadao = this.userAccountDao;

        ListSubjectTableFactory factory = new ListSubjectTableFactory();
        factory.setSubjectDao(sdao);
        factory.setStudySubjectDao(subdao);
        factory.setUserAccountDao(uadao);
        factory.setStudyDao(studyDao);
        factory.setCurrentStudy(currentStudy);


        String auditLogsHtml = factory.createTable(request, response).render();
        request.setAttribute("listSubjectsHtml", auditLogsHtml);

        forwardPage(Page.SUBJECT_LIST);
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

}
