/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2007 Akaza Research
 */

package org.researchedc.control.managestudy;

import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.bean.admin.AuditBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.Utils;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.submit.SubmitDataServlet;
import org.researchedc.dao.admin.AuditDAO;
import org.researchedc.dao.spi.AuditDao;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
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
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * @author jsampson
 * 
 */

public class ViewStudySubjectAuditLogServlet extends SecureController {

    
    @Autowired
    private AuditDao auditDao;
    @Autowired
    private CRFVersionDAO crfVersionDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;
    @Autowired
    private ISubjectDAO subjectDao;

/**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

//        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
//            return;
//        }
//        if (ub.isSysAdmin()) {
//            return;
//        }
//        Role r = currentRole.getRole();
//        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
//            return;
//        }
//        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
//        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS, resexception.getString("not_study_director"), "1");
        
    }

    @Override
    public void processRequest() throws Exception {
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

        ArrayList studySubjectAudits = new ArrayList();
        ArrayList eventCRFAudits = new ArrayList();
        ArrayList studyEventAudits = new ArrayList();
        ArrayList allDeletedEventCRFs = new ArrayList();
        String attachedFilePath = Utils.getAttachedFilePath(currentStudy);

        int studySubId = fp.getInt("id", true);// studySubjectId
        request.setAttribute("id", studySubId);

        if (studySubId == 0) {
            addPageMessage(respage.getString("please_choose_a_subject_to_view"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
        } else {
            StudySubjectBean studySubject = (StudySubjectBean) subdao.findByPK(studySubId);
            StudyBean study = (StudyBean) studydao.findByPK(studySubject.getStudyId());
            //Check if this StudySubject would be accessed from the Current Study
            if(studySubject.getStudyId() != currentStudy.getId()){
                if(currentStudy.getParentStudyId() > 0){
                    addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
                    forwardPage(Page.MENU_SERVLET);
                    return;
                } else {
                    // The SubjectStudy is not belong to currentstudy and current study is not a site.
                    Collection sites = studydao.findOlnySiteIdsByStudy(currentStudy);
                    if (!sites.contains(study.getId())) {
                        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
                        forwardPage(Page.MENU_SERVLET);
                        return;
                    }
                }
            }

            request.setAttribute("studySub", studySubject);
            SubjectBean subject = (SubjectBean) sdao.findByPK(studySubject.getSubjectId());

            IStudyParameterValueDAO spvdao = this.studyParameterValueDao;
            study.getStudyParameterConfig().setCollectDob(spvdao.findByHandleAndStudy(study.getId(), "collectDob").getValue());
            String collectdob="used";
                if (study.getStudyParameterConfig().getCollectDob().equals("2")) {
                	collectdob="yearOnly";
                }else if (study.getStudyParameterConfig().getCollectDob().equals("3")) {
                	collectdob="notUsed";                
                }else if (study.getStudyParameterConfig().getCollectDob().equals("1")) {
                	collectdob="used";                
                }
                
                
                
            request.setAttribute("collectdob", collectdob);
            request.setAttribute("subject", subject);

            request.setAttribute("study", study);

            /* Show both study subject and subject audit events together */
            // Study subject value changed
            Collection studySubjectAuditEvents = adao.findStudySubjectAuditEvents(studySubject.getId());
            // Text values will be shown on the page for the corresponding
            // integer values.
            for (Iterator iterator = studySubjectAuditEvents.iterator(); iterator.hasNext();) {
                AuditBean auditBean = (AuditBean) iterator.next();
                if (auditBean.getAuditEventTypeId() == 3) {
                    auditBean.setOldValue(Status.get(Integer.parseInt(auditBean.getOldValue())).getName());
                    auditBean.setNewValue(Status.get(Integer.parseInt(auditBean.getNewValue())).getName());
                }
            }
            studySubjectAudits.addAll(studySubjectAuditEvents);

            // Global subject value changed
            studySubjectAudits.addAll(adao.findSubjectAuditEvents(subject.getId()));

            studySubjectAudits.addAll(adao.findStudySubjectGroupAssignmentAuditEvents(studySubject.getId()));
            request.setAttribute("studySubjectAudits", studySubjectAudits);

            // Get the list of events
            ArrayList events = sedao.findAllByStudySubject(studySubject);
            for (int i = 0; i < events.size(); i++) {
                // Link study event definitions
                StudyEventBean studyEvent = (StudyEventBean) events.get(i);
                studyEvent.setStudyEventDefinition((StudyEventDefinitionBean) seddao.findByPK(studyEvent.getStudyEventDefinitionId()));

                // Link event CRFs
                studyEvent.setEventCRFs(ecdao.findAllByStudyEvent(studyEvent));

                // Find deleted Event CRFs
                List deletedEventCRFs = adao.findDeletedEventCRFsFromAuditEventByEventCRFStatus(studyEvent.getId());
                allDeletedEventCRFs.addAll(deletedEventCRFs);
                logger.info("deletedEventCRFs size[" + deletedEventCRFs.size() + "]");
            }

            for (int i = 0; i < events.size(); i++) {
                StudyEventBean studyEvent = (StudyEventBean) events.get(i);
                studyEventAudits.addAll(adao.findStudyEventAuditEvents(studyEvent.getId()));

                ArrayList eventCRFs = studyEvent.getEventCRFs();
                for (int j = 0; j < eventCRFs.size(); j++) {
                    // Link CRF and CRF Versions
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(j);
                    eventCRF.setCrfVersion((CRFVersionBean) cvdao.findByPK(eventCRF.getCRFVersionId()));
                    eventCRF.setCrf(cdao.findByVersionId(eventCRF.getCRFVersionId()));
                    // Get the event crf audits
                    eventCRFAudits.addAll(adao.findEventCRFAuditEventsWithItemDataType(eventCRF.getId()));
                    logger.info("eventCRFAudits size [" + eventCRFAudits.size() + "] eventCRF id [" + eventCRF.getId() + "]");
                }
            }
            ItemDataDAO itemDataDao = this.itemDataDao;
            for (Object o :eventCRFAudits) {
                AuditBean ab = (AuditBean)o;
                if (ab.getAuditTable().equalsIgnoreCase("item_data")) {
                    ItemDataBean idBean = (ItemDataBean)itemDataDao.findByPK(ab.getEntityId());
                    ab.setOrdinal(idBean.getOrdinal());
                }
            }
            request.setAttribute("events", events);
            request.setAttribute("eventCRFAudits", eventCRFAudits);
            request.setAttribute("studyEventAudits", studyEventAudits);
            request.setAttribute("allDeletedEventCRFs", allDeletedEventCRFs);
            request.setAttribute("attachedFilePath", attachedFilePath);

            forwardPage(Page.VIEW_STUDY_SUBJECT_AUDIT);

        }
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
