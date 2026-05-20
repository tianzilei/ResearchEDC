package org.researchedc.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.domain.datamap.EventCrfFlag;
import org.researchedc.domain.datamap.EventDefinitionCrfTag;
import org.researchedc.domain.datamap.EventDefinitionCrfItemTag;
import org.researchedc.domain.datamap.ItemDataFlag;
import org.researchedc.domain.datamap.ItemData;

public class EventCrfFlagDao extends AbstractDomainDao<EventCrfFlag> {

    @Override
    Class<EventCrfFlag> domainClass() {
        // TODO Auto-generated method stub
        return EventCrfFlag.class;
    }

    public EventCrfFlag findByEventCrfPath(int tagId, String path) {
        String query = "from " + getDomainClassName() + " where path = :path and tagId= :tagId";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("tagId", tagId);
        q.setParameter("path", path);
        
        return (EventCrfFlag) q.uniqueResult();

    }

}
