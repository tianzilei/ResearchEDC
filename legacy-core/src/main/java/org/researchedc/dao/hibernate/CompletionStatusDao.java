package org.researchedc.dao.hibernate;

import org.researchedc.domain.datamap.CompletionStatus;

public class CompletionStatusDao extends AbstractDomainDao<CompletionStatus> {

    @Override
    Class<CompletionStatus> domainClass() {
        // TODO Auto-generated method stub
        return CompletionStatus.class;
    }

    public CompletionStatus findByCompletionStatusId(int completion_status_id) {
        String query = "from " + getDomainClassName() + " completion_status  where completion_status.completionStatusId = :completionstatusid ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("completionstatusid", completion_status_id);
        return (CompletionStatus) q.uniqueResult();
    }


}
