package org.researchedc.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.domain.datamap.IdtView;
import org.researchedc.domain.datamap.ItemData;
import org.researchedc.domain.datamap.EventDefinitionCrfItemTag;

public class EventDefinitionCrfItemTagDao extends AbstractDomainDao<EventDefinitionCrfItemTag> {

    @Override
    Class<EventDefinitionCrfItemTag> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrfItemTag.class;
    }

    public List<EventDefinitionCrfItemTag> findAllByCrfPath(int tag_id, String crfPath, boolean active) {

        String query = " from " + getDomainClassName() + "  where " + " tag_id= " + tag_id + " and active=" + active + " and path LIKE '" + crfPath + ".%'";

        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        return (List<EventDefinitionCrfItemTag>) q.list();
    }

    public EventDefinitionCrfItemTag findByItemPath(int tag_id, boolean active, String itemPath) {

        String query = " from " + getDomainClassName() + "  where " + " tag_id= " + tag_id + " and active=" + active + " and path= '" + itemPath + "'";

        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        return (EventDefinitionCrfItemTag) q.uniqueResult();
    }

}
