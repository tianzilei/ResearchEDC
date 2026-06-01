package org.researchedc.bean.rule.action;
import javax.sql.DataSource;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
public class EmailActionProcessor implements ActionProcessor {
    public EmailActionProcessor(DataSource ds) {}
    public void execute(RuleActionBean ruleAction, int itemDataBeanId, String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {}
}
