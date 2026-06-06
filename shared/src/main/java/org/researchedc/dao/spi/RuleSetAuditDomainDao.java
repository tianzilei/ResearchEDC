package org.researchedc.dao.spi;

import org.researchedc.domain.rule.RuleSetAuditBean;
import org.researchedc.domain.rule.RuleSetBean;

import java.util.ArrayList;

public interface RuleSetAuditDomainDao {

    RuleSetAuditBean saveOrUpdate(RuleSetAuditBean ruleSetAuditBean);

    ArrayList<RuleSetAuditBean> findAllByRuleSet(RuleSetBean ruleSet);
}
