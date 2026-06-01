package org.researchedc.domain.rule.action;
import javax.sql.DataSource;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.logic.rulerunner.ExecutionMode;
import org.researchedc.logic.rulerunner.RuleRunner.RuleRunnerMode;
public class EmailActionProcessor implements ActionProcessor {
    public EmailActionProcessor(DataSource ds, Object mailSender, Object ruleActionRunLogDao, Object ruleSetRule) {}
    public RuleActionBean execute(RuleRunnerMode m, ExecutionMode e, RuleActionBean a, ItemDataBean i, String s, StudyBean st, UserAccountBean u, Object... args) { return a; }
}
