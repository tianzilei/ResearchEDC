package org.researchedc.dao.hibernate;

import org.researchedc.domain.user.UserType;

public class UserTypeDao extends AbstractDomainDao<UserType> {
	
    @Override
    public Class<UserType> domainClass() {
        return UserType.class;
    }
    
    public UserType findByUserTypeId(Integer userTypeId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userTypeId = :user_type_id";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("user_type_id", userTypeId);
        return (UserType) q.uniqueResult();
    }

}
