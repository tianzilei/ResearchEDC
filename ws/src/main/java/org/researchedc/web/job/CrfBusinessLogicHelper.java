package org.researchedc.web.job;

import java.util.ArrayList;
import java.util.Date;

import javax.sql.DataSource;

import org.researchedc.bean.core.DataEntryStage;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemDataDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Helper methods will be placed in this class - DRY
 */
public class CrfBusinessLogicHelper {

    // SessionManager sm;
    DataSource ds;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private EventDefinitionCRFDAO eventDefinitionCrfDao;
    @Autowired
    private EventCRFDAO eventCrfDao;
    @Autowired
    private ItemDataDAO itemDataDao;
    @Autowired
    private ItemDAO itemDao;
    @Autowired
    private IStudyDAO studyDao;
    @Autowired
    private StudyEventDAO studyEventDao;
    @Autowired
    private StudyEventDefinitionDAO studyEventDefinitionDao;
    @Autowired
    private ICrfDAO crfDao;

    // public CrfBusinessLogicHelper(SessionManager sm) {
    // this.sm = sm;
    // }

    public CrfBusinessLogicHelper(DataSource ds) {
        this.ds = ds;

    }

    private EventDefinitionCRFBean getEventDefinitionCrfByStudyEventAndCrfVersion(EventCRFBean eventCrf, StudyBean study) {

        // TODO we have to get that id before we can continue
        EventDefinitionCRFBean eventDefinitionCrf = eventDefinitionCrfDao.findByStudyEventIdAndCRFVersionId(study, eventCrf.getStudyEventId(),
                eventCrf.getCRFVersionId());
        return eventDefinitionCrf;
    }

    protected boolean isEachRequiredFieldFillout(EventCRFBean ecb) {
        int allRequiredNum = itemDao.findAllRequiredByCRFVersionId(ecb.getCRFVersionId());
        int allRequiredFilledOut = itemDataDao.findAllRequiredByEventCRFId(ecb);
        if (allRequiredNum > allRequiredFilledOut) {
            return false;
        }

        ArrayList allFilled = itemDataDao.findAllBlankRequiredByEventCRFId(ecb.getId(), ecb.getCRFVersionId());
        if (!allFilled.isEmpty()) {
            return false;
        }
        return true;
    }

    protected boolean areAllCompleted(StudyEventBean seBean, StudyBean study) {
        ArrayList allCRFs = eventCrfDao.findAllByStudyEvent(seBean);
        ArrayList allEDCs = (ArrayList) eventDefinitionCrfDao.findAllActiveByEventDefinitionId(study, seBean.getStudyEventDefinitionId());
        boolean eventCompleted = true;
        logger.info("found all crfs: " + allCRFs.size());
        logger.info("found all edcs: " + allEDCs.size());
        for (int i = 0; i < allCRFs.size(); i++) {
            EventCRFBean ec = (EventCRFBean) allCRFs.get(i);
            // logger.info("found a crf name from event crf bean: " +
            // ec.getCrf().getName());
            logger.info("found a event name from event crf bean: " + ec.getEventName() + " crf version id: " + ec.getCRFVersionId());
            if (!ec.getStatus().equals(Status.UNAVAILABLE) || allCRFs.size() < allEDCs.size()) {
                eventCompleted = false;
                break;
            }
        }
        logger.info("returning for are all completed: " + eventCompleted);
        return eventCompleted;
    }

    protected boolean areAllRequiredCompleted(StudyEventBean seBean, StudyBean study) {
        ArrayList allCRFs = eventCrfDao.findAllByStudyEvent(seBean);
        ArrayList allEDCs = (ArrayList) eventDefinitionCrfDao.findAllActiveByEventDefinitionId(study, seBean.getStudyEventDefinitionId());
        boolean eventRequiredCompleted = true;
        // keep in mind that allCRFs return only existing CRFs,
        // while allEDCs return all event defs in an event
        for (int i = 0; i < allCRFs.size(); i++) {
            EventCRFBean ec = (EventCRFBean) allCRFs.get(i);
            EventDefinitionCRFBean edcBean = eventDefinitionCrfDao.findByStudyEventIdAndCRFVersionId(study, seBean.getId(), ec.getCRFVersionId());
            if (!ec.getStatus().equals(Status.UNAVAILABLE) && edcBean.isRequiredCRF()) {
                // if it's not done but required, return FALSE
                eventRequiredCompleted = false;
                break;
            }
        }
        if (allCRFs.size() < allEDCs.size()) {
            // the above means that we didnt find all the edcs yet
            // iterate through edcs now, if we find a required one return FALSE,
            // since we found one that we didn't catch before
            for (int i = 0; i < allEDCs.size(); i++) {
                EventDefinitionCRFBean ec = (EventDefinitionCRFBean) allEDCs.get(i);
                if (ec.isRequiredCRF()) {
                    eventRequiredCompleted = false;
                    break;
                }
            }
        }
        logger.info("returning for event required completed: " + eventRequiredCompleted);
        return eventRequiredCompleted;
    }

    protected boolean noneAreRequired(StudyEventBean seBean, StudyBean study) {
        boolean noneAreRequired = true;
        ArrayList allEDCs = (ArrayList) eventDefinitionCrfDao.findAllActiveByEventDefinitionId(study, seBean.getStudyEventDefinitionId());
        logger.info("found all EDCs: " + allEDCs.size());
        for (int i = 0; i < allEDCs.size(); i++) {
            EventDefinitionCRFBean ec = (EventDefinitionCRFBean) allEDCs.get(i);
            logger.info("found crf name: " + ec.getCrfName());
            if (ec.isRequiredCRF()) {
                noneAreRequired = false;
                break;
            }
        }
        logger.info("returning for none are required: " + noneAreRequired);
        return noneAreRequired;
    }

    protected boolean areAllRequired(StudyEventBean seBean, StudyBean study) {
        ArrayList allEDCs = (ArrayList) eventDefinitionCrfDao.findAllActiveByEventDefinitionId(study, seBean.getStudyEventDefinitionId());
        boolean areAllRequired = true;
        for (int i = 0; i < allEDCs.size(); i++) {
            EventDefinitionCRFBean ec = (EventDefinitionCRFBean) allEDCs.get(i);
            if (!ec.isRequiredCRF()) {
                areAllRequired = false;
                break;
            }
        }
        logger.info("returning for are all required: " + areAllRequired);
        return areAllRequired;
    }

    /**
     * The following methods are for 'mark CRF complete' Note that we will also wrap Study Event status changes in this
     * code, possibly split out in a later release, tbh 06/2008
     * 
     * @return
     */
    public boolean markCRFComplete(EventCRFBean ecb, UserAccountBean ub, boolean inTransaction) throws Exception {
        DataEntryStage stage = ecb.getStage();
        StudyBean study = studyDao.findByStudySubjectId(ecb.getStudySubjectId());
        EventDefinitionCRFBean edcb = getEventDefinitionCrfByStudyEventAndCrfVersion(ecb, study);

        StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) studyEventDefinitionDao.findByPK(edcb.getStudyEventDefinitionId());
        ArrayList crfs = (ArrayList) crfDao.findAllActiveByDefinition(sedBean);
        sedBean.setCrfs(crfs);
        // request.setAttribute(TableOfContentsServlet.INPUT_EVENT_CRF_BEAN,
        // ecb);
        // request.setAttribute(INPUT_EVENT_CRF_ID, new
        // Integer(ecb.getId()));
        logger.debug("inout_event_crf_id:" + ecb.getId());
        logger.debug("inout_study_event_def_id:" + sedBean.getId());

        Status newStatus = ecb.getStatus();
        DataEntryStage newStage = ecb.getStage();
        boolean ide = true;

        newStatus = Status.UNAVAILABLE;
        // ecb.setUpdaterId(ub.getId());
        ecb.setUpdater(ub);
        ecb.setUpdatedDate(new Date());
        ecb.setDateCompleted(new Date());
        ecb.setDateValidateCompleted(new Date());

        /*
         * //for the non-reviewed sections, no item data in DB yet, need to //create them if
         * (!isEachSectionReviewedOnce()) { boolean canSave = saveItemsToMarkComplete(newStatus); if (canSave == false){
         * addPageMessage("You may not mark this Event CRF complete, because there are some required entries which have
         * not been filled out."); return false; } }
         */
        ecb.setStatus(newStatus);
        ecb.setStage(newStage);
        ecb = (EventCRFBean) eventCrfDao.update(ecb);
        logger.debug("just updated event crf id: " + ecb.getId());
        // note the below statement only updates the DATES, not the STATUS
        eventCrfDao.markComplete(ecb, ide);

        // update all the items' status to complete
        itemDataDao.updateStatusByEventCRF(ecb, newStatus);

        // change status for study event
        StudyEventBean seb = (StudyEventBean) studyEventDao.findByPK(ecb.getStudyEventId());
        seb.setUpdatedDate(new Date());
        seb.setUpdater(ub);

        // updates with Pauls observation from bug:2488:
        // 1. If there is only one CRF in the event (whether the CRF was
        // required or not), and data was imported for it, the status of the
        // event should be Completed.
        //
        logger.debug("sed bean get crfs get size: " + sedBean.getCrfs().size());
        logger.debug("edcb get crf id: " + edcb.getCrfId() + " version size? " + edcb.getVersions().size());
        logger.debug("ecb get crf id: " + ecb.getCrf().getId());
        logger.debug("ecb get crf version id: " + ecb.getCRFVersionId());

        if (sedBean.getCrfs().size() == 1) { // && edcb.getCrfId() ==
            // ecb.getCrf().getId()) {

            seb.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
            logger.info("just set subj event status to -- COMPLETED --");
        }
        // 2. More than one CRF in the event, all of them being required. If
        // data is imported into only one CRF, the status of the event
        // should be
        // Data Entry Started.
        //
        // removing sedBean.getCrfs().size() > 1 &&
        else if (areAllRequired(seb, study) && !areAllCompleted(seb, study)) {

            seb.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
            logger.info("just set subj event status to -- DATAENTRYSTARTED --");
        }
        // 3. More than one CRF in the event, one is required, the other is
        // not.
        // If data is imported into the Required CRF, the status of the
        // event
        // should be Completed.
        //
        // 4. More than one CRF in the event, one is required, the other is
        // not.
        // If data is imported into the non-required CRF, the status of the
        // event should be Data Entry Started.
        // tbh -- below case covers both
        // removing sedBean.getCrfs().size() > 1 &&
        else if (!areAllRequired(seb, study)) {
            if (areAllRequiredCompleted(seb, study)) {
                seb.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
                logger.info("just set subj event status to -- 3completed3 --");
            } else {
                seb.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
                logger.info("just set subj event status to -- DATAENTRYSTARTED --");
            }
        }

        // 5. More than one CRF in the event and none of them are required.
        // If
        // Data is imported into all the CRFs, the status of the event will
        // be
        // Completed. If the data is imported in some of the CRFs, the
        // status
        // will be completed as well.
        // removing sedBean.getCrfs().size() > 1 &&

        else if (noneAreRequired(seb, study)) {
            seb.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
            logger.info("just set subj event status to -- 5completed5 --");
        }
        logger.debug("just set subj event status, final status is " + seb.getSubjectEventStatus().getName());
        logger.debug("final overall status is " + seb.getStatus().getName());
            seb = (StudyEventBean) studyEventDao.update(seb,inTransaction);

        return true;
    }

    /**
     * The following methods are for 'mark CRF Started' Note that we will also wrap Study Event status changes in this
     * code, possibly split out in a later release, tbh 06/2008
     * 
     * @return
     */
    public boolean markCRFStarted(EventCRFBean ecb, UserAccountBean ub, boolean inTransaction) throws Exception {
        StudyBean study = studyDao.findByStudySubjectId(ecb.getStudySubjectId());
        EventDefinitionCRFBean edcb = getEventDefinitionCrfByStudyEventAndCrfVersion(ecb, study);

        StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) studyEventDefinitionDao.findByPK(edcb.getStudyEventDefinitionId());
        ArrayList crfs = (ArrayList) crfDao.findAllActiveByDefinition(sedBean);
        sedBean.setCrfs(crfs);

        logger.debug("inout_event_crf_id:" + ecb.getId());
        logger.debug("inout_study_event_def_id:" + sedBean.getId());

        Status newStatus = Status.AVAILABLE;
        DataEntryStage newStage = ecb.getStage();

        ecb.setUpdater(ub);
        ecb.setUpdatedDate(new Date());

        ecb.setStatus(newStatus);
        ecb.setStage(newStage);
        ecb = (EventCRFBean) eventCrfDao.update(ecb);
        logger.debug("just updated event crf id: " + ecb.getId());

        StudyEventBean seb = (StudyEventBean) studyEventDao.findByPK(ecb.getStudyEventId());
        if (seb.getSubjectEventStatus().isScheduled() || seb.getSubjectEventStatus().isNotScheduled() || seb.getSubjectEventStatus().isDE_Started()) {
            // change status for study event
            seb.setUpdatedDate(new Date());
            seb.setUpdater(ub);
            seb.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
        seb = (StudyEventBean) studyEventDao.update(seb,inTransaction);
        }

        return true;
    }

}
