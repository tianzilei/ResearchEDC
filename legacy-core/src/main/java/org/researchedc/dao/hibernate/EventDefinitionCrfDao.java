package org.researchedc.dao.hibernate;

import java.util.List;

import org.researchedc.domain.datamap.EventDefinitionCrf;

public class EventDefinitionCrfDao extends AbstractDomainDao<EventDefinitionCrf> {

    @Override
    Class<EventDefinitionCrf> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrf.class;
    }
    
    @SuppressWarnings("unchecked")
    public List<EventDefinitionCrf> findByStudyEventDefinitionId(int studyEventDefinitionId) {
        String query = "from "
                + getDomainClassName()
                + " event_definition_crf where event_definition_crf.studyEventDefinition.studyEventDefinitionId = :studyeventdefinitionid";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyeventdefinitionid", studyEventDefinitionId);
        return (List<EventDefinitionCrf>) q.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<EventDefinitionCrf> findAvailableByStudyEventDefStudy(Integer studyEventDefinitionId, Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.studyEventDefinition.studyEventDefinitionId = :studyeventdefid " + 
                " and do.study.studyId = :studyid and do.statusId = 1";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyeventdefid", studyEventDefinitionId);
        q.setParameter("studyid", studyId);
        return (List<EventDefinitionCrf>) q.list();
        
    }

    @SuppressWarnings("unchecked")
    public List<EventDefinitionCrf> findSiteHiddenByStudyEventDefStudy(Integer studyEventDefinitionId, Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.studyEventDefinition.studyEventDefinitionId = :studyeventdefid " + 
                " and do.study.studyId = :studyid and do.statusId = 1 and do.hideCrf = true";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyeventdefid", studyEventDefinitionId);
        q.setParameter("studyid", studyId);
        return (List<EventDefinitionCrf>) q.list();
        
    }
}
