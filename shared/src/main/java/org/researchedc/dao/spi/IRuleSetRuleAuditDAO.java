package org.researchedc.dao.spi;

import java.util.ArrayList;

import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.RuleSetRuleAuditBean;

public interface IRuleSetRuleAuditDAO {
    ArrayList<RuleSetRuleAuditBean> findAllByRuleSet(RuleSetBean ruleSet);
    RuleSetRuleAuditBean saveOrUpdate(RuleSetRuleAuditBean ruleSetRuleAuditBean);
}
