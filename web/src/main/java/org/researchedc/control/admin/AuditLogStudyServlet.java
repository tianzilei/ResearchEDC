/*
 * Created on Sep 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.researchedc.control.admin;

import org.researchedc.bean.core.Role;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.admin.AuditDAO;
import org.researchedc.dao.spi.AuditDao;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.managestudy.StudySubjectDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.SubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.researchedc.dao.admin.AuditEventDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * @author thickerson
 * 
 * 
 */
public class AuditLogStudyServlet extends SecureController {

    @Autowired
    protected AuditEventDAO auditEventDao;

    Locale locale;

    @Autowired
    private AuditDAO auditDao;
    @Autowired
    private CRFVersionDAO crfVersionDao;

    // <ResourceBundle resword,resexception,respage;

    public static String getLink(int userId) {
        return "AuditLogStudy";
    }

    /*
     * (non-Javadoc) Assume that we get the user id automatically. We will jump
     * from the edit user page if the user is an admin, they can get to see the
     * users' log
     * 
     * @see org.researchedc.control.core.SecureController#processRequest()
     */

    /*
     * (non-Javadoc) redo this servlet to run the audits per study subject for
     * the study; need to add a studyId param and then use the
     * IStudySubjectDAO.findAllByStudyOrderByLabel() method to grab a lot of
     * study subject beans and then return them much like in
     * ViewStudySubjectAuditLogServet.process()
     * 
     * currentStudy instead of studyId?
     */
    @Override
    protected void processRequest() throws Exception {
        int studyId = currentStudy.getId();

        IStudySubjectDAO subdao = this.studySubjectDao;
        ISubjectDAO sdao = this.subjectDao;
        AuditDao adao = this.auditDao;

        FormProcessor fp = new FormProcessor(request);

        IStudyEventDAO sedao = this.studyEventDao;
        IStudyEventDefinitionDAO seddao = this.studyEventDefinitionDao;
        EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;
        EventCRFDao ecdao = this.eventCrfDao;
        IStudyDAO studydao = this.studyDao;
        ICrfDAO cdao = this.crfDao;
        CRFVersionDAO cvdao = this.crfVersionDao;

        HashMap eventCRFAuditsHashMap = new HashMap();
        HashMap eventsHashMap = new HashMap();
        HashMap studySubjectAuditsHashMap = new HashMap();
        HashMap subjectHashMap = new HashMap();

        ArrayList studySubjects = subdao.findAllByStudyOrderByLabel(currentStudy);
        logger.info("found " + studySubjects.size() + " study subjects");
        request.setAttribute("studySubjects", studySubjects);

        for (int ss = 0; ss < studySubjects.size(); ss++) {
            ArrayList studySubjectAudits = new ArrayList();
            ArrayList eventCRFAudits = new ArrayList();

            StudySubjectBean studySubject = (StudySubjectBean) studySubjects.get(ss);
            // request.setAttribute("studySub"+ss, studySubject);
            SubjectBean subject = (SubjectBean) sdao.findByPK(studySubject.getSubjectId());
            subjectHashMap.put(Integer.valueOf(studySubject.getId()), subject);
            // logger.info("just set a subject with a status of
            // "+subject.getStatus().getName());
            // request.setAttribute("subject"+ss, subject);
            StudyBean study = (StudyBean) studydao.findByPK(studySubject.getStudyId());
            request.setAttribute("study", study);
            // hmm, repetitive work?

            // Show both study subject and subject audit events together
            studySubjectAudits.addAll(adao.findStudySubjectAuditEvents(studySubject.getId())); // Study
            // subject
            // value
            // changed
            studySubjectAudits.addAll(adao.findSubjectAuditEvents(subject.getId())); // Global
            // subject
            // value
            // changed

            studySubjectAuditsHashMap.put(Integer.valueOf(studySubject.getId()), studySubjectAudits);
            // request.setAttribute("studySubjectAudits"+ss,
            // studySubjectAudits);

            // Get the list of events
            ArrayList events = sedao.findAllByStudySubject(studySubject);
            for (int i = 0; i < events.size(); i++) {
                // Link study event definitions
                StudyEventBean studyEvent = (StudyEventBean) events.get(i);
                studyEvent.setStudyEventDefinition((StudyEventDefinitionBean) seddao.findByPK(studyEvent.getStudyEventDefinitionId()));

                // Link event CRFs
                studyEvent.setEventCRFs(ecdao.findAllByStudyEvent(studyEvent));
            }

            // for (int i = 0; i < events.size(); i++) {
            // StudyEventBean studyEvent = (StudyEventBean) events.get(i);
            // ArrayList eventCRFs = studyEvent.getEventCRFs();
            // for (int j = 0; j < eventCRFs.size(); j++) {
            // //Link CRF and CRF Versions
            // EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(j);
            // eventCRF.setCrfVersion((CRFVersionBean)
            // cvdao.findByPK(eventCRF.getCRFVersionId()));
            // eventCRF.setCrf((CRFBean)
            // cdao.findByVersionId(eventCRF.getCRFVersionId()));
            // //Get the event crf audits
            // eventCRFAudits.addAll(adao.findEventCRFAuditEvents(eventCRF.getId()));
            // }
            // }
            eventsHashMap.put(Integer.valueOf(studySubject.getId()), events);
            // request.setAttribute("events"+ss, events);
            // eventCRFAuditsHashMap.put(new Integer(studySubject.getId()),
            // eventCRFAudits);
            // request.setAttribute("eventCRFAudits"+ss, eventCRFAudits);
        }

        // request.setAttribute("eventCRFAudits", eventCRFAuditsHashMap);
        request.setAttribute("events", eventsHashMap);
        request.setAttribute("studySubjectAudits", studySubjectAuditsHashMap);
        request.setAttribute("study", currentStudy);
        request.setAttribute("subjects", subjectHashMap);

        // FormProcessor fp = new FormProcessor(request);
        //
        // IAuditEventDAO aeDAO = this.auditEventDao;
        // ArrayList al = aeDAO.findAllByStudyId(currentStudy.getId());
        //
        // EntityBeanTable table = fp.getEntityBeanTable();
        // ArrayList allRows = AuditEventStudyRow.generateRowsFromBeans(al);

        // String[] columns = { "Date and Time", "Action", "Entity/Operation",
        // "Record ID", "Changes and Additions","Other Info" };
        // table.setColumns(new ArrayList(Arrays.asList(columns)));
        // table.hideColumnLink(4);
        // table.hideColumnLink(1);
        // table.hideColumnLink(5);
        // table.setQuery("AuditLogUser?userLogId="+userId, new HashMap());
        // String[] columns =
        // {resword.getString("date_and_time"),resword.getString("action_message"),
        // resword.getString("entity_operation"),
        // resword.getString("updated_by"),resword.getString("subject_unique_ID"),resword.getString("changes_and_additions"),
        // //"Other Info",
        // resword.getString("actions")};
        // table.setColumns(new ArrayList(Arrays.asList(columns)));
        // table.setAscendingSort(false);
        // table.hideColumnLink(1);
        // table.hideColumnLink(5);
        // table.hideColumnLink(6);
        // //table.hideColumnLink(7);
        // table.setQuery("AuditLogStudy", new HashMap());
        // table.setRows(allRows);
        // table.computeDisplay();
        //
        //
        // request.setAttribute("table", table);

        logger.warn("*** found servlet, sending to page ***");
        String pattn = "";
        String pattern2 = "";
        pattn = ResourceBundleProvider.getFormatBundle().getString("date_format_string");
        pattern2 = ResourceBundleProvider.getFormatBundle().getString("date_time_format_string");
        request.setAttribute("dateFormatPattern", pattn);
        request.setAttribute("dateTimeFormatPattern", pattern2);
        forwardPage(Page.AUDIT_LOG_STUDY);

    }

    /*
     * (non-Javadoc) Since access to this servlet is admin-only, restricts user
     * to see logs of specific users only @author thickerson
     * 
     * @see org.researchedc.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        // <locale = request.getLocale();
        // <//<
        // resexception=ResourceBundle.getBundle("org.researchedc.i18n.exceptions",locale);
        // <//< respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",locale);
        // <//< resword =
        // ResourceBundle.getBundle("org.researchedc.i18n.words",locale);

        if (ub.isSysAdmin()) {
            return;
        }

        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");
    }

    // protected String getAdminServlet() {
    // return SecureController.ADMIN_SERVLET_CODE;
    // }

}
