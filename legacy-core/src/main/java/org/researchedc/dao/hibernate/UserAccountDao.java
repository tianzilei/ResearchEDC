package org.researchedc.dao.hibernate;

import org.researchedc.domain.user.UserAccount;

public class UserAccountDao extends AbstractDomainDao<UserAccount> {
	
    @Override
    public Class<UserAccount> domainClass() {
        return UserAccount.class;
    }
    
    public UserAccount findByUserName(String userName) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userName = :user_name";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("user_name", userName);
        return (UserAccount) q.uniqueResult();
    }

    public UserAccount findByUserId(Integer userId) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.userId = :user_id";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("user_id", userId);
        return (UserAccount) q.uniqueResult();
    }

}
