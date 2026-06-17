package org.researchedc.dao.spi;

import java.util.ArrayList;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.domain.rule.RuleBean;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.RuleSetRuleBean;

public interface IRuleSetRuleDAO {
    ArrayList<RuleSetRuleBean> findByRuleSetBeanAndRuleBean(RuleSetBean ruleSetBean, RuleBean ruleBean);
    ArrayList<RuleSetRuleBean> findByRuleSetStudyIdAndStatusAvail(Integer studyId);
    int getCountByStudy(StudyBean study);
    RuleSetRuleBean findById(Integer id);
    RuleSetRuleBean saveOrUpdate(RuleSetRuleBean ruleSetRuleBean);
}
