/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * @author jxu
 *
 * Restores a subject to system, also restore all the related data
 */
public class RestoreSubjectServlet extends SecureController {

    @Autowired
    private ISubjectDAO subjectDao;
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_frozen"));

        if (ub.isSysAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.SUBJECT_LIST_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        ISubjectDAO sdao = this.subjectDao;
        FormProcessor fp = new FormProcessor(request);
        int subjectId = fp.getInt("id");

        String action = fp.getString("action");
        if (subjectId == 0 || StringUtil.isBlank(action)) {
            addPageMessage(respage.getString("please_choose_a_subject_to_restore"));
            forwardPage(Page.SUBJECT_LIST_SERVLET);
        } else {

            SubjectBean subject = (SubjectBean) sdao.findByPK(subjectId);

            // find all study subjects
            IStudySubjectDAO ssdao = this.studySubjectDao;
            ArrayList studySubs = ssdao.findAllBySubjectId(subjectId);

            // find study events
            IStudyEventDAO sedao = this.studyEventDao;
            ArrayList events = sedao.findAllBySubjectId(subjectId);
            if ("confirm".equalsIgnoreCase(action)) {
                request.setAttribute("subjectToRestore", subject);
                request.setAttribute("studySubs", studySubs);
                request.setAttribute("events", events);
                forwardPage(Page.RESTORE_SUBJECT);
            } else {
                logger.info("submit to restore the subject");
                // change all statuses to AVAILABLE
                subject.setStatus(Status.AVAILABLE);
                subject.setUpdater(ub);
                subject.setUpdatedDate(new Date());
                sdao.update(subject);

                // remove subject references from study
                for (int i = 0; i < studySubs.size(); i++) {
                    StudySubjectBean studySub = (StudySubjectBean) studySubs.get(i);
                    if (studySub.getStatus().equals(Status.AUTO_DELETED)) {
                        studySub.setStatus(Status.AVAILABLE);
                        studySub.setUpdater(ub);
                        studySub.setUpdatedDate(new Date());
                        ssdao.update(studySub);
                    }
                }

                EventCRFDao ecdao = this.eventCrfDao;

                for (int j = 0; j < events.size(); j++) {
                    StudyEventBean event = (StudyEventBean) events.get(j);
                    if (event.getStatus().equals(Status.AUTO_DELETED)) {
                        event.setStatus(Status.AVAILABLE);
                        event.setUpdater(ub);
                        event.setUpdatedDate(new Date());
                        sedao.update(event);

                        ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

                        IItemDataDAO iddao = this.itemDataDao;
                        for (int k = 0; k < eventCRFs.size(); k++) {
                            EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                            if (eventCRF.getStatus().equals(Status.AUTO_DELETED)) {
                                eventCRF.setStatus(Status.AVAILABLE);
                                eventCRF.setUpdater(ub);
                                eventCRF.setUpdatedDate(new Date());
                                ecdao.update(eventCRF);
                                // restore all the item data
                                ArrayList itemDatas = iddao.findAllByEventCRFId(eventCRF.getId());
                                for (int a = 0; a < itemDatas.size(); a++) {
                                    ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                                    if (item.getStatus().equals(Status.AUTO_DELETED)) {
                                        item.setStatus(Status.AVAILABLE);
                                        item.setUpdater(ub);
                                        item.setUpdatedDate(new Date());
                                        iddao.update(item);
                                    }
                                }
                            }
                        }
                    }
                }

                String emailBody = respage.getString("the_subject") + subject.getName() + " " + respage.getString("has_been_restored_succesfully");

                addPageMessage(emailBody);
//                sendEmail(emailBody);

                forwardPage(Page.SUBJECT_LIST_SERVLET);

            }
        }

    }

    /**
     * Send email to administrator
     *
     * @param request
     * @param response
     */
    private void sendEmail(String emailBody) throws Exception {

        logger.info("Sending email...");
        // to admin
        sendEmail(ub.getEmail().trim(), respage.getString("restore_subject_to_system"), emailBody, false);
        logger.info("Sending email done..");
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
