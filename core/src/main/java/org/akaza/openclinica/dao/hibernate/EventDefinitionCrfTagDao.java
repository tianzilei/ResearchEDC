package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;

public class EventDefinitionCrfTagDao extends AbstractDomainDao<EventDefinitionCrfTag> {

    @Override
    Class<EventDefinitionCrfTag> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrfTag.class;
    }

    public EventDefinitionCrfTag findByCrfPath(int tagId, String path, boolean active) {
        String query = "from " + getDomainClassName() + " where path = :path and tagId= :tagId and active= :active ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("tagId", tagId);
        q.setParameter("path", path);
        q.setParameter("active", active);
        return (EventDefinitionCrfTag) q.uniqueResult();

    }

}
