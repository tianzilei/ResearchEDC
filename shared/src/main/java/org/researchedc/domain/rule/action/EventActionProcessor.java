package org.researchedc.domain.rule.action;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.logic.rulerunner.ExecutionMode;
import org.researchedc.logic.rulerunner.RuleRunner.RuleRunnerMode;

/**
 * 
 * @author jnyayapathi
 *
 */
public class EventActionProcessor implements ActionProcessor {

	@Override
	public RuleActionBean execute(RuleRunnerMode ruleRunnerMode,
			ExecutionMode executionMode, RuleActionBean ruleAction,
			ItemDataBean itemDataBean, String itemData, StudyBean currentStudy,
			UserAccountBean ub, Object... arguments) {
		// TODO Auto-generated method stub
		return null;
	}

}
