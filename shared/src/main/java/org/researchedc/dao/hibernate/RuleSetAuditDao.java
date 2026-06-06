package org.researchedc.dao.hibernate;

import org.researchedc.domain.rule.RuleSetAuditBean;
import org.researchedc.dao.spi.RuleSetAuditDomainDao;
import org.researchedc.domain.rule.RuleSetBean;

import java.util.ArrayList;

public class RuleSetAuditDao extends AbstractDomainDao<RuleSetAuditBean> implements RuleSetAuditDomainDao {

    @Override
    public Class<RuleSetAuditBean> domainClass() {
        return RuleSetAuditBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        String query = "from " + getDomainClassName() + " ruleSetAudit  where ruleSetAudit.ruleSetBean = :ruleSet  ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("ruleSet", ruleSet);
        return (ArrayList<RuleSetAuditBean>) q.list();
    }
}
