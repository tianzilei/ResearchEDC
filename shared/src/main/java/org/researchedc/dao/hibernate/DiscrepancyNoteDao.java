package org.researchedc.dao.hibernate;

import java.util.List;

import org.researchedc.domain.datamap.DiscrepancyNote;

public class DiscrepancyNoteDao extends AbstractDomainDao<DiscrepancyNote> {

    @Override
    Class<DiscrepancyNote> domainClass() {
        return DiscrepancyNote.class;
    }

    public List<DiscrepancyNote> findParentNotesByItemData(Integer itemDataId) {
        String query = "select dn.* from discrepancy_note dn, dn_item_data_map didm where didm.item_data_id=" + itemDataId + " AND dn.parent_dn_id isnull " + 
            "AND dn.discrepancy_note_id=didm.discrepancy_note_id";
        org.hibernate.query.Query q = getCurrentSession().createNativeQuery(query, DiscrepancyNote.class);
        return ((List<DiscrepancyNote>) q.list());
    }

    public DiscrepancyNote findByDiscrepancyNoteId(int discrepancyNoteId) {
        String query = "from " + getDomainClassName() + " do where do.discrepancyNoteId = :discrepancynoteid ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("discrepancynoteid", discrepancyNoteId);
        return (DiscrepancyNote) q.uniqueResult();
    }


}
