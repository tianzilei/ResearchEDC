package org.researchedc.dao.hibernate;

import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemGroupMetadataBean;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.domain.crfdata.DynamicsItemGroupMetadataBean;

public class DynamicsItemGroupMetadataDao extends AbstractDomainDao<DynamicsItemGroupMetadataBean>{

    @Override 
    public Class<DynamicsItemGroupMetadataBean> domainClass() {
        return DynamicsItemGroupMetadataBean.class;
    }
    
    public DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, EventCRFBean eventCrfBean) {
        String query =
            "from " + getDomainClassName()
                + " metadata where metadata.itemGroupMetadataId = :id and metadata.itemGroupId = :item_group_id and metadata.eventCrfId = :event_crf_id ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("id", Integer.valueOf(metadataBean.getId()));
        q.setParameter("item_group_id", Integer.valueOf(metadataBean.getItemGroupId()));
        q.setParameter("event_crf_id", Integer.valueOf(eventCrfBean.getId()));
        return (DynamicsItemGroupMetadataBean) q.uniqueResult();
    }
    
    public DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, int eventCrfBeanId) {
        String query =
            "from " + getDomainClassName()
                + " metadata where metadata.itemGroupMetadataId = :id and metadata.itemGroupId = :item_group_id and metadata.eventCrfId = :event_crf_id ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("id", Integer.valueOf(metadataBean.getId()));
        q.setParameter("item_group_id", Integer.valueOf(metadataBean.getItemGroupId()));
        q.setParameter("event_crf_id", Integer.valueOf(eventCrfBeanId));
        return (DynamicsItemGroupMetadataBean) q.uniqueResult();
    }
    
    public Boolean hasShowingInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String query = "";
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            query = "select dg.item_group_id from dyn_item_group_metadata dg where dg.event_crf_id = :eventCrfId and dg.item_group_metadata_id in ("
                + " select distinct igm.item_group_metadata_id from item_group_metadata igm where igm.crf_version_id = :crfVersionId"
                + " and igm.show_group = 0"
                + " and igm.item_id in (select im.item_id from item_form_metadata im where im.section_id = :sectionId and im.crf_version_id = :crfVersionId))"
                + " and dg.show_group = 1 and rownum = 1";
        } else {
        query = "select dg.item_group_id from dyn_item_group_metadata dg where dg.event_crf_id = :eventCrfId and dg.item_group_metadata_id in ("
                + " select distinct igm.item_group_metadata_id from item_group_metadata igm where igm.crf_version_id = :crfVersionId"
                + " and igm.show_group = 'false'"
                + " and igm.item_id in (select im.item_id from item_form_metadata im where im.section_id = :sectionId and im.crf_version_id = :crfVersionId))"
                + " and dg.show_group = 'true' limit 1";
        }
        
        org.hibernate.query.Query q = this.getCurrentSession().createNativeQuery(query);
        q.setParameter("eventCrfId", eventCrfId);
        q.setParameter("crfVersionId", crfVersionId);
        q.setParameter("sectionId", sectionId);
        q.setParameter("crfVersionId", crfVersionId);
        return q.list() != null && q.list().size() > 0;
    }
    public  void delete(int eventCrfId){
        String query = " delete from " + getDomainClassName() +  "  where eventCrfId =:eventCrfId ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("eventCrfId", eventCrfId);
        q.executeUpdate();
    }

}
