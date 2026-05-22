package org.researchedc.ws.internal.adapter;

import java.util.List;

import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.springframework.stereotype.Repository;

@Repository
public class EventDefinitionCRFAdapter {

    private final EventDefinitionCRFDAO delegate;

    public EventDefinitionCRFAdapter(EventDefinitionCRFDAO delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    public List<EventDefinitionCRFBean> findAllByDefinition(StudyBean study, int definitionId) {
        return (List<EventDefinitionCRFBean>) delegate.findAllByDefinition(study, definitionId);
    }

    public EventDefinitionCRFBean findByStudyEventIdAndCRFVersionId(StudyBean studyBean, int studyEventId, int crfVersionId) {
        return (EventDefinitionCRFBean) delegate.findByStudyEventIdAndCRFVersionId(studyBean, studyEventId, crfVersionId);
    }

    @SuppressWarnings("unchecked")
    public List<EventDefinitionCRFBean> findAllActiveByEventDefinitionId(StudyBean study, int eventDefinitionId) {
        return (List<EventDefinitionCRFBean>) delegate.findAllActiveByEventDefinitionId(study, eventDefinitionId);
    }

    public EventDefinitionCRFDAO getDelegate() {
        return delegate;
    }
}
