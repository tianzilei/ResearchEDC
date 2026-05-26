/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.DisplayStudyEventBean;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.DisplayEventCRFBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.core.EmailEngine;
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
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * @author jxu
 *
 * Restores a removed study event and all its data
 */
public class RestoreStudyEventServlet extends SecureController {
    
    @Autowired
    private CRFVersionDAO crfVersionDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;

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

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int studyEventId = fp.getInt("id");// studyEventId
        int studySubId = fp.getInt("studySubId");// studySubjectId

        IStudyEventDAO sedao = this.studyEventDao;
        IStudySubjectDAO subdao = this.studySubjectDao;

        if (studyEventId == 0) {
            addPageMessage(respage.getString("please_choose_a_SE_to_restore"));
            request.setAttribute("id", Integer.valueOf(studySubId).toString());
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);

            // YW 11-07-2007, a study event could not be restored if its study
            // subject has been removed
            Status s = studySub.getStatus();
            if ("removed".equalsIgnoreCase(s.getName()) || "auto-removed".equalsIgnoreCase(s.getName())) {
                addPageMessage(resword.getString("study_event") + resterm.getString("could_not_be") + resterm.getString("restored") + "."
                    + respage.getString("study_subject_has_been_deleted"));
                request.setAttribute("id", Integer.valueOf(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
            // YW

            StudyEventBean event = (StudyEventBean) sedao.findByPK(studyEventId);

            request.setAttribute("studySub", studySub);

            IStudyEventDefinitionDAO seddao = this.studyEventDefinitionDao;
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
            event.setStudyEventDefinition(sed);

            IStudyDAO studydao = this.studyDao;
            StudyBean study = (StudyBean) studydao.findByPK(studySub.getStudyId());
            request.setAttribute("subStudy", study);

            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {
                if (event.getStatus().equals(Status.AVAILABLE)) {
                    addPageMessage(respage.getString("this_event_is_already_available_for_study") + " "
                        + respage.getString("please_contact_sysadmin_for_more_information"));
                    request.setAttribute("id", Integer.valueOf(studySubId).toString());
                    forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
                    return;
                }

                EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;
                // find all crfs in the definition
                ArrayList eventDefinitionCRFs = (ArrayList) edcdao.findAllByEventDefinitionId(study, sed.getId());

                EventCRFDao ecdao = this.eventCrfDao;
                ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

                // construct info needed on view study event page
                DisplayStudyEventBean de = new DisplayStudyEventBean();
                de.setStudyEvent(event);
                de.setDisplayEventCRFs(getDisplayEventCRFs(eventCRFs, eventDefinitionCRFs));

                request.setAttribute("displayEvent", de);

                forwardPage(Page.RESTORE_STUDY_EVENT);
            } else {
                logger.info("submit to restore the event to study");
                // restore event to study
                event.setStatus(Status.AVAILABLE);
                event.setUpdater(ub);
                event.setUpdatedDate(new Date());
                sedao.update(event);

                // restore event crfs
                EventCRFDao ecdao = this.eventCrfDao;

                ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

                IItemDataDAO iddao = this.itemDataDao;
                for (int k = 0; k < eventCRFs.size(); k++) {
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                    if (eventCRF.getStatus().equals(Status.AUTO_DELETED)) {
                        eventCRF.setStatus(Status.AVAILABLE);
                        eventCRF.setUpdater(ub);
                        eventCRF.setUpdatedDate(new Date());
                        ecdao.update(eventCRF);
                        // remove all the item data
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

                String emailBody =
                    respage.getString("the_event") + event.getStudyEventDefinition().getName() + " " + respage.getString("has_been_restored_to_the_study")
                        + " " + study.getName() + ".";

                addPageMessage(emailBody);
//                sendEmail(emailBody);
                request.setAttribute("id", Integer.valueOf(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
    }

    /**
     * Each of the event CRFs with its corresponding CRFBean. Then generates a
     * list of DisplayEventCRFBeans, one for each event CRF.
     *
     * @param eventCRFs
     *            The list of event CRFs for this study event.
     * @param eventDefinitionCRFs
     *            The list of event definition CRFs for this study event.
     * @return The list of DisplayEventCRFBeans for this study event.
     */
    private ArrayList getDisplayEventCRFs(ArrayList eventCRFs, ArrayList eventDefinitionCRFs) {
        ArrayList answer = new ArrayList();

        HashMap definitionsById = new HashMap();
        int i;
        for (i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
            definitionsById.put(Integer.valueOf(edc.getStudyEventDefinitionId()), edc);
        }

        IStudyEventDAO sedao = this.studyEventDao;
        ICrfDAO cdao = this.crfDao;
        CRFVersionDAO cvdao = this.crfVersionDao;

        for (i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecb = (EventCRFBean) eventCRFs.get(i);

            // populate the event CRF with its crf bean
            int crfVersionId = ecb.getCRFVersionId();
            CRFBean cb = cdao.findByVersionId(crfVersionId);
            ecb.setCrf(cb);

            CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            ecb.setCrfVersion(cvb);

            // then get the definition so we can call
            // DisplayEventCRFBean.setFlags
            int studyEventId = ecb.getStudyEventId();
            int studyEventDefinitionId = sedao.getDefinitionIdFromStudyEventId(studyEventId);

            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) definitionsById.get(Integer.valueOf(studyEventDefinitionId));

            DisplayEventCRFBean dec = new DisplayEventCRFBean();
            dec.setFlags(ecb, ub, currentRole, edc.isDoubleEntry());
            answer.add(dec);
        }

        return answer;
    }

    /**
     * Send email to director and administrator
     *
     * @param request
     * @param response
     */
    private void sendEmail(String emailBody) throws Exception {

        logger.info("Sending email...");
        // to study director
        sendEmail(ub.getEmail().trim(), respage.getString("restore_event_to_study"), emailBody, false);
        // to admin
        sendEmail(EmailEngine.getAdminEmail(), respage.getString("restore_event_to_study"), emailBody, false, false);
        logger.info("Sending email done..");
    }

}
