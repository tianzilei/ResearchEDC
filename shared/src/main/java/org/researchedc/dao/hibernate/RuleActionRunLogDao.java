package org.researchedc.dao.hibernate;

import org.researchedc.domain.rule.action.RuleActionRunLogBean;
import org.researchedc.dao.spi.RuleActionRunLogDomainDao;
import org.springframework.transaction.annotation.Transactional;

public class RuleActionRunLogDao extends AbstractDomainDao<RuleActionRunLogBean> implements RuleActionRunLogDomainDao {

    @Override
    public Class<RuleActionRunLogBean> domainClass() {
        return RuleActionRunLogBean.class;
    }

    @Transactional
    public Integer findCountByRuleActionRunLogBean(RuleActionRunLogBean ruleActionRunLog) {
        String query = "select count(*) from " + getDomainClassName() + " where id = :id";
        Long k = (Long) getCurrentSession().createQuery(query)
            .setParameter("id", ruleActionRunLog.getId())
            .uniqueResult();
        return k.intValue();
    }

    public void delete(int itemDataId) {
        String query = "delete from " + getDomainClassName() + " where itemDataId = :itemDataId";
        getCurrentSession().createQuery(query)
            .setParameter("itemDataId", itemDataId)
            .executeUpdate();
    }

}
