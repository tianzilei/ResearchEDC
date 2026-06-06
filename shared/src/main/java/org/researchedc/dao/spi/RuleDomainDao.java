package org.researchedc.dao.spi;

import org.researchedc.domain.rule.RuleBean;

public interface RuleDomainDao {

    RuleBean saveOrUpdate(RuleBean ruleBean);

    RuleBean findByOid(RuleBean ruleBean);

    RuleBean findByOid(String oid, Integer studyId);
}
