/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;
import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.ResolutionStatus;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.*;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.DisplayEventCRFBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.core.EmailEngine;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.managestudy.*;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.spi.IItemDataDAO;

/**
 * Removes an Event CRF
 * 
 * @author jxu
 * 
 */
public class RemoveEventCRFServlet extends SecureController {
    
    @Autowired
    private CRFVersionDAO crfVersionDao;
    @Autowired
    private IDiscrepancyNoteDAO discrepancyNoteDao;
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
        int eventCRFId = fp.getInt("id");// eventCRFId
        int studySubId = fp.getInt("studySubId");// studySubjectId
        checkStudyLocked("ViewStudySubject?id" + studySubId, respage.getString("current_study_locked"));
        IStudyEventDAO sedao = this.studyEventDao;
        IStudySubjectDAO subdao = this.studySubjectDao;
        EventCRFDao ecdao = this.eventCrfDao;
        IStudyDAO sdao = this.studyDao;

        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_remove"));
            request.setAttribute("id", Integer.valueOf(studySubId).toString());
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {
            EventCRFBean eventCRF = (EventCRFBean) ecdao.findByPK(eventCRFId);

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            request.setAttribute("studySub", studySub);

            // construct info needed on view event crf page
            ICrfDAO cdao = this.crfDao;
            CRFVersionDAO cvdao = this.crfVersionDao;

            int crfVersionId = eventCRF.getCRFVersionId();
            CRFBean cb = cdao.findByVersionId(crfVersionId);
            eventCRF.setCrf(cb);

            CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            eventCRF.setCrfVersion(cvb);

            // then get the definition so we can call
            // DisplayEventCRFBean.setFlags
            int studyEventId = eventCRF.getStudyEventId();

            StudyEventBean event = (StudyEventBean) sedao.findByPK(studyEventId);

            int studyEventDefinitionId = sedao.getDefinitionIdFromStudyEventId(studyEventId);
            IStudyEventDefinitionDAO seddao = this.studyEventDefinitionDao;
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(studyEventDefinitionId);
            event.setStudyEventDefinition(sed);
            request.setAttribute("event", event);

            EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;

            StudyBean study = (StudyBean) sdao.findByPK(studySub.getStudyId());
            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());

            DisplayEventCRFBean dec = new DisplayEventCRFBean();
            dec.setEventCRF(eventCRF);
            dec.setFlags(eventCRF, ub, currentRole, edc.isDoubleEntry());

            // find all item data
            IItemDataDAO iddao = this.itemDataDao;

            ArrayList itemData = iddao.findAllByEventCRFId(eventCRF.getId());

            request.setAttribute("items", itemData);

            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {
                if (eventCRF.getStatus().equals(Status.DELETED) || eventCRF.getStatus().equals(Status.AUTO_DELETED)) {
                    addPageMessage(respage.getString("this_event_CRF_is_removed_for_this_study") + " "
                        + respage.getString("please_contact_sysadmin_for_more_information"));
                    request.setAttribute("id", Integer.valueOf(studySubId).toString());
                    forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
                    return;
                }

                request.setAttribute("displayEventCRF", dec);

                forwardPage(Page.REMOVE_EVENT_CRF);
            } else {
                logger.info("submit to remove the event CRF from study");

                eventCRF.setStatus(Status.DELETED);
                eventCRF.setUpdater(ub);
                eventCRF.setUpdatedDate(new Date());
                ecdao.update(eventCRF);

                // remove all the item data
                for (int a = 0; a < itemData.size(); a++) {
                    ItemDataBean item = (ItemDataBean) itemData.get(a);
                    if (!item.getStatus().equals(Status.DELETED)) {
                        item.setStatus(Status.AUTO_DELETED);
                        item.setUpdater(ub);
                        item.setUpdatedDate(new Date());
                        iddao.update(item);
                        DiscrepancyNoteDAO dnDao = (DiscrepancyNoteDAO) this.discrepancyNoteDao;
                        List dnNotesOfRemovedItem = dnDao.findExistingNotesForItemData(item.getId());
                        if (!dnNotesOfRemovedItem.isEmpty()) {
                            DiscrepancyNoteBean itemParentNote = null;
                            for (Object obj : dnNotesOfRemovedItem) {
                                if (((DiscrepancyNoteBean)obj).getParentDnId() == 0) {
                                    itemParentNote = (DiscrepancyNoteBean)obj;
                                }
                            }
                            DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
                            if (itemParentNote != null) {
                                dnb.setParentDnId(itemParentNote.getId());
                                dnb.setDiscrepancyNoteTypeId(itemParentNote.getDiscrepancyNoteTypeId());
                            }
                            dnb.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
                            dnb.setStudyId(currentStudy.getId());
                            dnb.setAssignedUserId(ub.getId());
                            dnb.setOwner(ub);
                            dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
                            dnb.setEntityId(item.getId());
                            dnb.setColumn("value");
                            dnb.setCreatedDate(new Date());
                            dnb.setDescription("The item has been removed, this Discrepancy Note has been Closed.");
                            dnDao.create(dnb);
                            dnDao.createMapping(dnb);
                            itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
                            dnDao.update(itemParentNote);
                        }
                    }
                }

                String emailBody =
                    respage.getString("the_event_CRF") + " " + cb.getName() + " " + respage.getString("has_been_removed_from_the_event")
                        + event.getStudyEventDefinition().getName() + ". "+ respage.getString("has_been_removed_from_the_event_cont");

                addPageMessage(emailBody);
                sendEmail(emailBody);
                request.setAttribute("id", Integer.valueOf(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
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

        sendEmail(ub.getEmail().trim(), respage.getString("remove_event_CRF_from_event"), emailBody, false);
 //       sendEmail(EmailEngine.getAdminEmail(), respage.getString("remove_event_CRF_from_event"), emailBody, false);
        logger.info("Sending email done..");
    }

}
