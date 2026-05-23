/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.submit;

import org.researchedc.bean.core.DataEntryStage;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.submit.DisplayTableOfContentsBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.service.crfdata.DynamicsMetadataService;
import org.researchedc.view.Page;
import org.researchedc.web.InconsistentStateException;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * @author ssachs
 */
public class MarkEventCRFCompleteServlet extends SecureController {

    @Autowired
    protected SectionDAO sectionDao;

    Locale locale;
    // < ResourceBundleresexception,respage,resword;

    public static final String INPUT_EVENT_CRF_ID = "eventCRFId";

    public static final String INPUT_MARK_COMPLETE = "markComplete";

    public static final String VALUE_YES = "Yes";

    public static final String VALUE_NO = "No";

    public static final String BEAN_DISPLAY = "toc";

    private FormProcessor fp;

    private EventCRFDao ecdao;

    private EventCRFBean ecb;

    private EventDefinitionCRFDao edcdao;

    private EventDefinitionCRFBean edcb;

    private void getEventCRFBean() {
        // if ((fp != null) && (ecdao != null) && (ecb != null)) {
        // return ;
        // }

        fp = new FormProcessor(request);
        int eventCRFId = fp.getInt(INPUT_EVENT_CRF_ID);

        ecdao = this.eventCrfDao;
        ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);
    }

    private boolean isEachRequiredFieldFillout() {
        ItemDataDAO iddao = this.itemDataDao;
        ArrayList dataList = iddao.findAllBlankRequiredByEventCRFId(ecb.getId(), ecb.getCRFVersionId());
        // empty means all required fields got filled out,return true-jxu
        return dataList.isEmpty();
    }

    private boolean isEachSectionReviewedOnce() {
        SectionDAO sdao = this.sectionDao;

        DataEntryStage stage = ecb.getStage();

        ArrayList sections = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());
        HashMap numItemsHM = sdao.getNumItemsBySectionId();
        HashMap numItemsPendingHM = sdao.getNumItemsPendingBySectionId(ecb);
        HashMap numItemsCompletedHM = sdao.getNumItemsCompletedBySectionId(ecb);

        for (int i = 0; i < sections.size(); i++) {
            SectionBean sb = (SectionBean) sections.get(i);
            Integer key = new Integer(sb.getId());

            int numItems = TableOfContentsServlet.getIntById(numItemsHM, key);
            int numItemsPending = TableOfContentsServlet.getIntById(numItemsPendingHM, key);
            int numItemsCompleted = TableOfContentsServlet.getIntById(numItemsCompletedHM, key);

            if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY) && edcb.isDoubleEntry()) {
                if (numItemsPending == 0 && numItems > 0) {
                    return false;
                }
            } else {
                if (numItemsCompleted == 0 && numItems > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private void getEventDefinitionCRFBean() {
        edcdao = this.eventDefinitionCrfDao;
        edcb = edcdao.findForStudyByStudyEventIdAndCRFVersionId(ecb.getStudyEventId(), ecb.getCRFVersionId());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
        // FormDiscrepancyNotes discNotes =
        // (FormDiscrepancyNotes)session.getAttribute(AddNewSubjectServlet.
        // FORM_DISCREPANCY_NOTES_NAME);
        getEventCRFBean();
        getEventDefinitionCRFBean();
        DataEntryStage stage = ecb.getStage();

        request.setAttribute(TableOfContentsServlet.INPUT_EVENT_CRF_BEAN, ecb);
        // Page errorPage = Page.TABLE_OF_CONTENTS_SERVLET;
        Page errorPage = Page.LIST_STUDY_SUBJECTS_SERVLET;

        /*
         * if (StringUtil.isBlank(ecb.getInterviewerName())) { if ((discNotes ==
         * null) ||
         * discNotes.getNotes(TableOfContentsServlet.INPUT_INTERVIEWER).
         * isEmpty()){ throw new InconsistentStateException(errorPage, "You may
         * not mark this Event CRF complete, because interviewer name is
         * blank."); } }
         *
         * if (ecb.getDateInterviewed() == null) { if ((discNotes == null) ||
         * (discNotes
         * .getNotes(TableOfContentsServlet.INPUT_INTERVIEW_DATE).isEmpty()) ) {
         * throw new InconsistentStateException(errorPage, "You may not mark
         * this Event CRF complete, because interview date is blank."); } }
         */

        if (stage.equals(DataEntryStage.UNCOMPLETED) || stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) || stage.equals(DataEntryStage.LOCKED)) {
            throw new InconsistentStateException(errorPage, respage.getString("not_mark_CRF_complete1"));
        }

        if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) || stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {

            if (!edcb.isDoubleEntry()) {
                throw new InconsistentStateException(errorPage, respage.getString("not_mark_CRF_complete2"));
            }
        }

        /*
         * if (!isEachSectionReviewedOnce()) { throw new
         * InconsistentStateException( errorPage, "You may not mark this Event
         * CRF complete, because there are some sections which have not been
         * reviewed once."); }
         */

        if (!isEachRequiredFieldFillout()) {
            throw new InconsistentStateException(errorPage, respage.getString("not_mark_CRF_complete4"));
        }

        if (ecb.getInterviewerName().trim().equals("")) {
            throw new InconsistentStateException(errorPage, respage.getString("not_mark_CRF_complete5"));
        }

        if (!fp.isSubmitted()) {
            DisplayTableOfContentsBean toc = TableOfContentsServlet.getDisplayBean(ecb, sm.getDataSource(), currentStudy);
            toc = TableOfContentsServlet.getDisplayBeanWithShownSections(sm.getDataSource(), toc,
                    (DynamicsMetadataService)SpringServletAccess.getApplicationContext(getServletContext()).getBean("dynamicsMetadataService"));
            request.setAttribute(BEAN_DISPLAY, toc);

            resetPanel();
            panel.setStudyInfoShown(false);
            panel.setOrderedData(true);
            setToPanel(resword.getString("subject"), toc.getStudySubject().getLabel());
            setToPanel(resword.getString("study_event_definition"), toc.getStudyEventDefinition().getName());

            StudyEventBean seb = toc.getStudyEvent();
            setToPanel(resword.getString("location"), seb.getLocation());
            setToPanel(resword.getString("start_date"), seb.getDateStarted().toString());
            setToPanel(resword.getString("end_date"), seb.getDateEnded().toString());

            setToPanel(resword.getString("CRF"), toc.getCrf().getName());
            setToPanel(resword.getString("CRF_version"), toc.getCrfVersion().getName());

            forwardPage(Page.MARK_EVENT_CRF_COMPLETE);
        } else {
            boolean markComplete = fp.getString(INPUT_MARK_COMPLETE).equals(VALUE_YES);
            if (markComplete) {
                Status newStatus = ecb.getStatus();
                boolean ide = true;
                if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY) && edcb.isDoubleEntry()) {
                    newStatus = Status.PENDING;
                    ecb.setUpdaterId(ub.getId());
                    ecb.setUpdatedDate(new Date());
                    ecb.setDateCompleted(new Date());
                } else if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY) && !edcb.isDoubleEntry()) {
                    newStatus = Status.UNAVAILABLE;
                    ecb.setUpdaterId(ub.getId());
                    ecb.setUpdatedDate(new Date());
                    ecb.setDateCompleted(new Date());
                    ecb.setDateValidateCompleted(new Date());
                } else if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) || stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
                    newStatus = Status.UNAVAILABLE;
                    ecb.setDateValidateCompleted(new Date());
                    ide = false;
                }
                ecb.setStatus(newStatus);
                ecb = (EventCRFBean) ecdao.update(ecb);
                ecdao.markComplete(ecb, ide);

                ItemDataDAO iddao = this.itemDataDao;
                iddao.updateStatusByEventCRF(ecb, newStatus);

                // change status for event
                IStudyEventDAO sedao = this.studyEventDao;
                StudyEventBean seb = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
                seb.setUpdatedDate(new Date());
                seb.setUpdater(ub);

                EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;
                ArrayList allCRFs = ecdao.findAllByStudyEvent(seb);
                ArrayList allEDCs = edcdao.findAllActiveByEventDefinitionId(seb.getStudyEventDefinitionId());
                boolean eventCompleted = true;
                for (int i = 0; i < allCRFs.size(); i++) {
                    EventCRFBean ec = (EventCRFBean) allCRFs.get(i);
                    if (!ec.getStatus().equals(Status.UNAVAILABLE)) {
                        eventCompleted = false;
                        break;
                    }
                }
                if (eventCompleted && allCRFs.size() >= allEDCs.size()) {
                    seb.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
                }

                seb = (StudyEventBean) sedao.update(seb);

                addPageMessage(respage.getString("event_CRF_marked_complete"));
                request.setAttribute(EnterDataForStudyEventServlet.INPUT_EVENT_ID, String.valueOf(ecb.getStudyEventId()));
                forwardPage(Page.ENTER_DATA_FOR_STUDY_EVENT_SERVLET);
            } else {
                request.setAttribute(DataEntryServlet.INPUT_IGNORE_PARAMETERS, Boolean.TRUE);
                addPageMessage(respage.getString("event_CRF_not_marked_complete"));
                forwardPage(errorPage);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // <
        // resexception=ResourceBundle.getBundle(
        // "org.researchedc.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",
        // locale);
        // < resword =
        // ResourceBundle.getBundle("org.researchedc.i18n.words",locale);

        fp = new FormProcessor(request);

        if (currentRole.equals(Role.COORDINATOR) || currentRole.equals(Role.STUDYDIRECTOR)) {
            return;
        }

        getEventCRFBean();

        Role r = currentRole.getRole();
        if (ecb.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY)) {
            if (ecb.getOwnerId() != ub.getId() && !r.equals(Role.COORDINATOR) && !r.equals(Role.STUDYDIRECTOR)) {
                request.setAttribute(TableOfContentsServlet.INPUT_EVENT_CRF_BEAN, ecb);
                addPageMessage(respage.getString("not_mark_CRF_complete6"));
                throw new InsufficientPermissionException(Page.TABLE_OF_CONTENTS_SERVLET, resexception.getString("not_study_owner"), "1");
            }
        } else if (ecb.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
            if (ecb.getValidatorId() != ub.getId() && !r.equals(Role.COORDINATOR) && !r.equals(Role.STUDYDIRECTOR)) {
                request.setAttribute(TableOfContentsServlet.INPUT_EVENT_CRF_BEAN, ecb);
                addPageMessage(respage.getString("not_mark_CRF_complete7"));
                throw new InsufficientPermissionException(Page.TABLE_OF_CONTENTS_SERVLET, resexception.getString("not_study_owner"), "1");
            }
        }

        return;
    }
}
