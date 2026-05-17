package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.EventCrf;

public class EventCrfDao extends AbstractDomainDao<EventCrf> {

    @Override
    Class<EventCrf> domainClass() {
        // TODO Auto-generated method stub
        return EventCrf.class;
    }

    public EventCrf findByStudyEventIdStudySubjectIdCrfVersionId(int study_event_id, int study_subject_id, int crf_version_id) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.crfVersion.crfVersionId = :crfversionid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyeventid", study_event_id);
        q.setParameter("studysubjectid", study_subject_id);
        q.setParameter("crfversionid", crf_version_id);
        return (EventCrf) q.uniqueResult();
    }

    public EventCrf findByStudyEventIdStudySubjectIdCrfId(int study_event_id, int study_subject_id, int crf_id) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.crfVersion.crf.crfId = :crfid and event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.studySubjectId= :studysubjectid";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyeventid", study_event_id);
        q.setParameter("studysubjectid", study_subject_id);
        q.setParameter("crfid", crf_id);
        return (EventCrf) q.uniqueResult();
    }

    @SuppressWarnings("unchecked")
	public List<EventCrf> findByStudyEventIdStudySubjectId(Integer studyEventId, String studySubjectOid) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.studySubject.ocOid= :studysubjectoid";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyeventid", studyEventId);
        q.setParameter("studysubjectoid", studySubjectOid);
        return q.list();
	}
    
    @SuppressWarnings("unchecked")
    public List<EventCrf> findByStudyEventStatus(Integer studyEventId, Integer statusCode) {
        String query = "from "
                + getDomainClassName()
                + " event_crf where event_crf.studyEvent.studyEventId = :studyeventid and event_crf.statusId = :statusid";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyeventid", studyEventId);
        q.setParameter("statusid", statusCode);
        return q.list();
    }
    
        
}
