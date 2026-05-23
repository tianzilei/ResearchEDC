package org.researchedc.dao.hibernate;

import org.researchedc.domain.datamap.ResolutionStatus;

public class ResolutionStatusDao extends AbstractDomainDao<ResolutionStatus> {

    @Override
    public Class<ResolutionStatus> domainClass() {
        return ResolutionStatus.class;
    }
    public ResolutionStatus findByResolutionStatusId(Integer resolutionStatusId) {
        String query = "from " + getDomainClassName() + " do  where do.resolutionStatusId = :resolutionstatusid";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("resolutionstatusid", resolutionStatusId);
        return (ResolutionStatus) q.uniqueResult();
    }

}
