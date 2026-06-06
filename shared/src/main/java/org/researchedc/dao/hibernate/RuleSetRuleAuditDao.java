package org.researchedc.dao.hibernate;

import org.researchedc.dao.spi.IRuleSetRuleAuditDAO;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.RuleSetRuleAuditBean;

import java.util.ArrayList;

public class RuleSetRuleAuditDao extends AbstractDomainDao<RuleSetRuleAuditBean> implements IRuleSetRuleAuditDAO {

    @Override
    public Class<RuleSetRuleAuditBean> domainClass() {
        return RuleSetRuleAuditBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetRuleAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        String query = "from " + getDomainClassName() + " ruleSetRuleAudit  where ruleSetRuleAudit.ruleSetRuleBean.ruleSetBean = :ruleSet  ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("ruleSet", ruleSet);
        return (ArrayList<RuleSetRuleAuditBean>) q.list();
    }
}
