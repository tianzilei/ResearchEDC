package org.researchedc.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.domain.datamap.EventCrfFlag;
import org.researchedc.domain.datamap.ItemDataFlag;
import org.researchedc.domain.datamap.ItemData;

public class ItemDataFlagDao extends AbstractDomainDao<ItemDataFlag> {

    @Override
    Class<ItemDataFlag> domainClass() {
        // TODO Auto-generated method stub
        return ItemDataFlag.class;
    }


    
    public List<ItemDataFlag> findAllByEventCrfPath(int tag_id , String eventCrfPath ) {
    	
        String query = " from " + getDomainClassName() + "  where "
                + " tag_id = :tag_id and path LIKE :eventCrfPath";
        
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("tag_id", tag_id);
        q.setParameter("eventCrfPath", eventCrfPath + ".%");
        
        return (List<ItemDataFlag>) q.list();
    }

    public ItemDataFlag findByItemDataPath(int tag_id ,  String itemDataPath ) {

        String query = " from " + getDomainClassName() + "  where "
                + " tag_id= :tag_id  and path= :itemDataPath ";
        
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("tag_id", tag_id);
        q.setParameter("itemDataPath", itemDataPath);
        
        return (ItemDataFlag) q.uniqueResult();
    }


    
}
