/*
 * Created on Sep 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.researchedc.control.managestudy;

import org.researchedc.bean.core.Role;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.admin.AuditDAO;
import org.researchedc.dao.spi.AuditDao;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author thickerson
 * 
 * 
 */
public class StudyAuditLogServlet extends SecureController {

    
    @Autowired
    private AuditDao auditDao;
    @Autowired
    private CRFVersionDAO crfVersionDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;
    @Autowired
    private ISubjectDAO subjectDao;
    @Autowired
    private IUserAccountDAO userAccountDao;

Locale locale;

    // <ResourceBundle resword,resexception,respage;

    public static String getLink(int userId) {
        return "AuditLogStudy";
    }

    /*
     * (non-Javadoc) Assume that we get the user id automatically. We will jump
     * from the edit user page if the user is an admin, they can get to see the
     * users' log
     * @see org.researchedc.control.core.SecureController#processRequest()
     */

    /*
     * (non-Javadoc) redo this servlet to run the audits per study subject for
     * the study; need to add a studyId param and then use the
     * IStudySubjectDAO.findAllByStudyOrderByLabel() method to grab a lot of
     * study subject beans and then return them much like in
     * ViewStudySubjectAuditLogServet.process() currentStudy instead of studyId?
     */
    @Override
    protected void processRequest() throws Exception {
        int studyId = currentStudy.getId();

        IStudySubjectDAO subdao = this.studySubjectDao;
        ISubjectDAO sdao = this.subjectDao;
        AuditDao adao = this.auditDao;
        IUserAccountDAO uadao = this.userAccountDao;

        FormProcessor fp = new FormProcessor(request);

        IStudyEventDAO sedao = this.studyEventDao;
        IStudyEventDefinitionDAO seddao = this.studyEventDefinitionDao;
        EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;
        EventCRFDao ecdao = this.eventCrfDao;
        IStudyDAO studydao = this.studyDao;
        ICrfDAO cdao = this.crfDao;
        CRFVersionDAO cvdao = this.crfVersionDao;

        StudyAuditLogTableFactory factory = new StudyAuditLogTableFactory();
        factory.setSubjectDao(sdao);
        factory.setStudySubjectDao(subdao);
        factory.setUserAccountDao(uadao);
        factory.setCurrentStudy(currentStudy);

        String auditLogsHtml = factory.createTable(request, response).render();
        request.setAttribute("auditLogsHtml", auditLogsHtml);

        forwardPage(Page.AUDIT_LOGS_STUDY);

    }

    /*
     * (non-Javadoc) Since access to this servlet is admin-only, restricts user
     * to see logs of specific users only @author thickerson
     * @see org.researchedc.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        if (ub.isSysAdmin()) {
            return;
        }

        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");
    }

}
