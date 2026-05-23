package org.researchedc.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.domain.datamap.ItemGroupMetadata;
import org.hibernate.query.Query;

public class ItemGroupMetadataDao extends AbstractDomainDao<ItemGroupMetadata> {

    @Override
    Class<ItemGroupMetadata> domainClass() {
        // TODO Auto-generated method stub
        return ItemGroupMetadata.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ItemGroupMetadata> findByItemGroupCrfVersion(Integer itemGroupId, Integer crfVersionId) {
        String query = "select distinct igm.* from item_group_metadata igm, item_group ig where igm.crf_version_id = " + String.valueOf(crfVersionId)
                + " and ig.item_group_id = igm.item_group_id and ig.item_group_id = " + String.valueOf(itemGroupId) + " order by igm.ordinal asc";
        org.hibernate.query.Query q = getCurrentSession().createNativeQuery(query, ItemGroupMetadata.class);
        return (ArrayList<ItemGroupMetadata>) q.list();
    }

    public ItemGroupMetadata findByItemCrfVersion(int item_id, int crf_version_id) {
        String query = "from " + getDomainClassName() + " do where do.item.itemId = :itemid and do.crfVersion.crfVersionId = :crfversionid";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("itemid", item_id);
        q.setParameter("crfversionid", crf_version_id);
        return (ItemGroupMetadata) q.uniqueResult();
    }

    public static final String findAllByCrfVersionQuery = "select distinct * from item_group_metadata igm where igm.crf_version_id = :crfversionid";

    @SuppressWarnings("unchecked")
    public List<ItemGroupMetadata> findAllByCrfVersion(int crf_version_id) {
        Query q = getCurrentSession().createNativeQuery(findAllByCrfVersionQuery, ItemGroupMetadata.class);
        q.setParameter("crfversionid", crf_version_id);
        return (List<ItemGroupMetadata>) q.list();
    }
}
