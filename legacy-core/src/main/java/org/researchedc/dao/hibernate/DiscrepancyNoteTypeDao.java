package org.researchedc.dao.hibernate;

import org.researchedc.domain.datamap.DiscrepancyNoteType;

public class DiscrepancyNoteTypeDao extends AbstractDomainDao<DiscrepancyNoteType> {

    @Override
    public Class<DiscrepancyNoteType> domainClass() {
        return DiscrepancyNoteType.class;
    }
    public DiscrepancyNoteType findByDiscrepancyNoteTypeId(Integer discrepancyNoteTypeId) {
        String query = "from " + getDomainClassName() + " do  where do.discrepancyNoteTypeId = :discrepancynotetypeid";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("discrepancynotetypeid", discrepancyNoteTypeId);
        return (DiscrepancyNoteType) q.uniqueResult();
    }

}
