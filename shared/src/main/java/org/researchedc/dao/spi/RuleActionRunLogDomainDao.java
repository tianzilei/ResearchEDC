package org.researchedc.dao.spi;

import org.researchedc.domain.rule.action.RuleActionRunLogBean;

public interface RuleActionRunLogDomainDao {

    RuleActionRunLogBean saveOrUpdate(RuleActionRunLogBean ruleActionRunLog);

    Integer findCountByRuleActionRunLogBean(RuleActionRunLogBean ruleActionRunLog);

    void delete(int itemDataId);
}
