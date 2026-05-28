package org.researchedc.service;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;

public class ParticipantEventService {

    private DataSource dataSource = null;
    private IStudyDAO studyDAO = null;
    private StudyEventDAO studyEventDAO = null;
    private EventCRFDAO eventCRFDAO = null;
    private EventDefinitionCRFDAO eventDefCRFDAO = null;
    private CRFVersionDAO crfVersionDAO = null;
    
    public ParticipantEventService(DataSource dataSource) { 
        this.dataSource = dataSource;
    }

    public ParticipantEventService(IStudyDAO studyDAO, StudyEventDAO studyEventDAO, EventCRFDAO eventCRFDAO,
            EventDefinitionCRFDAO eventDefCRFDAO, CRFVersionDAO crfVersionDAO) {
        this.studyDAO = studyDAO;
        this.studyEventDAO = studyEventDAO;
        this.eventCRFDAO = eventCRFDAO;
        this.eventDefCRFDAO = eventDefCRFDAO;
        this.crfVersionDAO = crfVersionDAO;
    }
    
    public StudyEventBean getNextParticipantEvent(StudySubjectBean studySubject) {
        List<StudyEventBean> studyEvents = (ArrayList<StudyEventBean>)getStudyEventDAO().findAllBySubjectIdOrdered(studySubject.getId());
        
        for (StudyEventBean studyEvent:studyEvents) {
            // Skip to next event if study event is not in the right status
            if (studyEvent.getStatus() != Status.AVAILABLE || 
                    (studyEvent.getSubjectEventStatus() != SubjectEventStatus.DATA_ENTRY_STARTED
                    && studyEvent.getSubjectEventStatus() != SubjectEventStatus.SCHEDULED)) continue;
            
            List<EventDefinitionCRFBean> eventDefCrfs = getEventDefCrfsForStudyEvent(studySubject, studyEvent);
            
            for (EventDefinitionCRFBean eventDefCrf:eventDefCrfs) {
                boolean participantForm = eventDefCrf.isParticipantForm();
                
                if (participantForm) {
                    List<CRFVersionBean> crfVersions = getAllCrfVersions(eventDefCrf);
                    
                    boolean eventCrfExists = false;
                    for (CRFVersionBean crfVersion:crfVersions) {
                        EventCRFBean eventCRF = getEventCRFDAO().findByEventCrfVersion(studyEvent, crfVersion);
                        if (eventCRF != null && eventCRF.getStatus() == Status.AVAILABLE) return studyEvent;
                        else if (eventCRF != null) eventCrfExists = true;
                    }
                    if (!eventCrfExists) return studyEvent;
                    
                }
            }
        }
        
        // Did not find a next participant event
        return null;
    }
    
    public EventCRFBean getExistingEventCRF(StudySubjectBean studySubject, StudyEventBean nextEvent,
            EventDefinitionCRFBean eventDefCrf) {

        List<CRFVersionBean> crfVersions = getAllCrfVersions(eventDefCrf);
        for (CRFVersionBean crfVersion:crfVersions) {
            EventCRFBean eventCRF = (EventCRFBean) getEventCRFDAO().findByEventCrfVersion(nextEvent, crfVersion);
            if (eventCRF != null) return eventCRF;
        }
        return null;
    }

    public List<CRFVersionBean> getAllCrfVersions(EventDefinitionCRFBean eventDefCrf) {

        List<CRFVersionBean> versions = new ArrayList<CRFVersionBean>();
        
        EventDefinitionCRFBean selectedEventDefCrf = null;
        if (eventDefCrf.getParentId() > 0) selectedEventDefCrf = (EventDefinitionCRFBean)getEventDefCRFDAO().findByPK(eventDefCrf.getParentId());
        else selectedEventDefCrf = eventDefCrf;
        versions = (ArrayList) getCRFVersionDAO().findAllByCRF(selectedEventDefCrf.getCrfId());
        
        return versions;
       }

    public List<EventDefinitionCRFBean> getEventDefCrfsForStudyEvent(StudySubjectBean studySubject, StudyEventBean studyEvent) {
        Integer studyId = studySubject.getStudyId();
        StudyBean studyBean = (StudyBean) getStudyDAO().findByPK(studyId);
        ArrayList<EventDefinitionCRFBean> eventDefCrfs = null;
        ArrayList<EventDefinitionCRFBean> parentEventDefCrfs = new ArrayList<EventDefinitionCRFBean>();
        ArrayList<EventDefinitionCRFBean> netEventDefinitionCrfs = new ArrayList<EventDefinitionCRFBean>();

        eventDefCrfs = (ArrayList<EventDefinitionCRFBean>) getEventDefCRFDAO().findAllDefIdandStudyId(studyEvent.getStudyEventDefinitionId(), studyId);

        StudyBean parentStudy = null;
        if (studyBean.getParentStudyId() == 0) parentStudy = studyBean;
        else parentStudy = (StudyBean) getStudyDAO().findByPK(studyBean.getParentStudyId());
        parentEventDefCrfs = (ArrayList<EventDefinitionCRFBean>) getEventDefCRFDAO().findAllDefIdandStudyId(studyEvent.getStudyEventDefinitionId(), parentStudy.getId());

        boolean found;
        for (EventDefinitionCRFBean parentEventDefinitionCrf : parentEventDefCrfs) {
            found = false;
            for (EventDefinitionCRFBean eventDefinitionCrf : eventDefCrfs) {
                if (parentEventDefinitionCrf.getId() == eventDefinitionCrf.getParentId()) { //
                    found = true;
                    netEventDefinitionCrfs.add(eventDefinitionCrf);
                    break;
                }
            }
            if (!found) netEventDefinitionCrfs.add(parentEventDefinitionCrf);
        }

        return netEventDefinitionCrfs;
    }

    /**
     * @return the StudyDAO
     */
    private IStudyDAO getStudyDAO() {
        return studyDAO;
    }

    /**
     * @return the StudyEventDAO
     */
    private StudyEventDAO getStudyEventDAO() {
        return studyEventDAO;
    }

    /**
     * @return the EventCRFDAO
     */
    private EventCRFDAO getEventCRFDAO() {
        return eventCRFDAO;
    }

    /**
     * @return the EventDefinitionCRFDAO
     */
    private EventDefinitionCRFDAO getEventDefCRFDAO() {
        return eventDefCRFDAO;
    }

    /**
     * @return the CRFVersionDAO
     */
    private CRFVersionDAO getCRFVersionDAO() {
        return crfVersionDAO;
    }

}
