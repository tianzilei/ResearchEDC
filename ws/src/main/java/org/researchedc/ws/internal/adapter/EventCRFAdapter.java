package org.researchedc.ws.internal.adapter;

import java.util.ArrayList;

import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.dao.spi.EventCRFDao;
import org.springframework.stereotype.Repository;

@Repository
public class EventCRFAdapter {

    private final EventCRFDao delegate;

    public EventCRFAdapter(EventCRFDao delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<EventCRFBean> findByEventSubjectVersion(StudyEventBean studyEvent, StudySubjectBean studySubject, CRFVersionBean crfVersion) {
        return delegate.findByEventSubjectVersion(studyEvent, studySubject, crfVersion);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<EventCRFBean> findByEventSubjectCRFid(StudyEventBean studyEvent, StudySubjectBean studySubject, CRFVersionBean crfVersion) {
        return delegate.findByEventSubjectCRFid(studyEvent, studySubject, crfVersion);
    }

    public EventCRFBean findByEventCrfVersion(StudyEventBean studyEvent, CRFVersionBean crfVersion) {
        return (EventCRFBean) delegate.findByEventCrfVersion(studyEvent, crfVersion);
    }

    public EventCRFBean findByEventCrfID(StudyEventBean studyEvent, CRFVersionBean crfVersion) {
        return (EventCRFBean) delegate.findByEventCrfID(studyEvent, crfVersion);
    }

    @SuppressWarnings("unchecked")
    public EventCRFBean create(EventCRFBean eventCRFBean) {
        return (EventCRFBean) delegate.create(eventCRFBean);
    }

    public EventCRFBean findByPK(int id) {
        return (EventCRFBean) delegate.findByPK(id);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<EventCRFBean> findAllByStudyEvent(StudyEventBean studyEvent) {
        return delegate.findAllByStudyEvent(studyEvent);
    }

    public EventCRFBean update(EventCRFBean bean) {
        return (EventCRFBean) delegate.update(bean);
    }

    public void markComplete(EventCRFBean ecb, boolean ide) {
        delegate.markComplete(ecb, ide);
    }

    public void updateCRFVersionID(int eventCrfId, int crfVersionId, int userId) {
        delegate.updateCRFVersionID(eventCrfId, crfVersionId, userId);
    }

    public void setSDVStatus(boolean sdvStatus, int userId, int eventCrfId) {
        delegate.setSDVStatus(sdvStatus, userId, eventCrfId);
    }

    public EventCRFDao getDelegate() {
        return delegate;
    }
}
