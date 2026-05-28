package org.researchedc.service;

import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;

import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.core.SessionManager;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("legacyEventService")
public class EventService implements EventServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    ISubjectDAO subjectDao;
    IStudySubjectDAO studySubjectDao;
    IUserAccountDAO userAccountDao;
    IStudyEventDefinitionDAO studyEventDefinitionDao;
    IStudyEventDAO studyEventDao;
    IStudyDAO studyDao;
    DataSource dataSource;

    public EventService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EventService(SessionManager sessionManager) {
        this.dataSource = sessionManager.getDataSource();
    }

    @Autowired
    public EventService(
            DataSource dataSource,
            ISubjectDAO subjectDao,
            IStudyDAO studyDao,
            IStudySubjectDAO studySubjectDao,
            IUserAccountDAO userAccountDao,
            IStudyEventDefinitionDAO studyEventDefinitionDao,
            IStudyEventDAO studyEventDao) {
        this.dataSource = dataSource;
        this.subjectDao = subjectDao;
        this.studyDao = studyDao;
        this.studySubjectDao = studySubjectDao;
        this.userAccountDao = userAccountDao;
        this.studyEventDefinitionDao = studyEventDefinitionDao;
        this.studyEventDao = studyEventDao;
    }

    public HashMap<String, String> scheduleEvent(UserAccountBean user, Date startDateTime, Date endDateTime, String location, String studyUniqueId,
            String siteUniqueId, String eventDefinitionOID, String studySubjectId) throws OpenClinicaSystemException {

        // Business Validation
        StudyBean study = getStudyDao().findByUniqueIdentifier(studyUniqueId);
        int parentStudyId = study.getId();
        if (siteUniqueId != null) {
            study = getStudyDao().findSiteByUniqueIdentifier(studyUniqueId, siteUniqueId);
        }
        StudyEventDefinitionBean studyEventDefinition = getStudyEventDefinitionDao().findByOidAndStudy(eventDefinitionOID, study.getId(), parentStudyId);
        StudySubjectBean studySubject = getStudySubjectDao().findByLabelAndStudy(studySubjectId, study);

        Integer studyEventOrdinal = null;
        if (canSubjectScheduleAnEvent(studyEventDefinition, studySubject)) {

            StudyEventBean studyEvent = new StudyEventBean();
            studyEvent.setStudyEventDefinitionId(studyEventDefinition.getId());
            studyEvent.setStudySubjectId(studySubject.getId());
            studyEvent.setLocation(location);
            studyEvent.setDateStarted(startDateTime);
            studyEvent.setDateEnded(endDateTime);
            studyEvent.setOwner(user);
            studyEvent.setStatus(Status.AVAILABLE);
            studyEvent.setSubjectEventStatus(SubjectEventStatus.SCHEDULED);
            studyEvent.setSampleOrdinal(getStudyEventDao().getMaxSampleOrdinal(studyEventDefinition, studySubject) + 1);
            studyEvent = (StudyEventBean) getStudyEventDao().create(studyEvent, true);
            studyEventOrdinal = studyEvent.getSampleOrdinal();

        } else {
            throw new OpenClinicaSystemException("Cannot schedule an event for this Subject");
        }

        HashMap<String, String> h = new HashMap<String, String>();
        h.put("eventDefinitionOID", eventDefinitionOID);
        h.put("studyEventOrdinal", studyEventOrdinal.toString());
        h.put("studySubjectOID", studySubject.getOid());
        return h;

    }

    public boolean canSubjectScheduleAnEvent(StudyEventDefinitionBean studyEventDefinition, StudySubjectBean studySubject) {

        if (studyEventDefinition.isRepeating()) {
            return true;
        }
        if (getStudyEventDao().findAllByDefinitionAndSubject(studyEventDefinition, studySubject).size() > 0) {
            return false;
        }
        return true;
    }

    /**
     * @return the subjectDao
     */
    public ISubjectDAO getSubjectDao() {
        return subjectDao;
    }

    /**
     * @return the subjectDao
     */
    public IStudyDAO getStudyDao() {
        return studyDao;
    }

    /**
     * @return the subjectDao
     */
    public IStudySubjectDAO getStudySubjectDao() {
        return studySubjectDao;
    }

    /**
     * @return the UserAccountDao
     */
    public IUserAccountDAO getUserAccountDao() {
        return userAccountDao;
    }

    /**
     * @return the StudyEventDefinitionDao
     */
    public IStudyEventDefinitionDAO getStudyEventDefinitionDao() {
        return studyEventDefinitionDao;
    }

    /**
     * @return the StudyEventDao
     */
    public IStudyEventDAO getStudyEventDao() {
        return studyEventDao;
    }

    /**
     * @return the datasource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param datasource
     *            the datasource to set
     */
    public void setDatasource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
