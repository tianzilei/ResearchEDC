package org.researchedc.domain.rule.action;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.dao.hibernate.RuleActionRunLogDao;
import org.researchedc.domain.rule.RuleSetRuleBean;
import org.researchedc.logic.rulerunner.ExecutionMode;
import org.researchedc.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.researchedc.service.managestudy.DiscrepancyNoteService;

import javax.sql.DataSource;

public class DiscrepancyNoteActionProcessor implements ActionProcessor {

    DataSource ds;
    DiscrepancyNoteService discrepancyNoteService;
    RuleActionRunLogDao ruleActionRunLogDao;
    RuleSetRuleBean ruleSetRule;

    public DiscrepancyNoteActionProcessor(DataSource ds, RuleActionRunLogDao ruleActionRunLogDao, RuleSetRuleBean ruleSetRule) {
        this.ds = ds;
        this.ruleActionRunLogDao = ruleActionRunLogDao;
        this.ruleSetRule = ruleSetRule;
    }

    public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean,
            String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {
        switch (executionMode) {
        case DRY_RUN: {
            return dryRun(ruleAction, itemDataBean, itemData, currentStudy, ub);
        }

        case SAVE: {
            return save(ruleAction, itemDataBean, itemData, currentStudy, ub);
        }
        default:
            return null;
        }
    }

    private RuleActionBean save(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        getDiscrepancyNoteService().saveFieldNotes(ruleAction.getCuratedMessage(), itemDataBean.getId(), itemData, currentStudy, ub);
        RuleActionRunLogBean ruleActionRunLog =
            new RuleActionRunLogBean(ruleAction.getActionType(), itemDataBean, itemDataBean.getValue(), ruleSetRule.getRuleBean().getOid());
        ruleActionRunLogDao.saveOrUpdate(ruleActionRunLog);
        return null;
    }

    private RuleActionBean dryRun(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        return ruleAction;
    }

    public void execute(String message, int itemDataBeanId, String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {
        getDiscrepancyNoteService().saveFieldNotes(message, itemDataBeanId, itemData, currentStudy, ub);
    }

    private DiscrepancyNoteService getDiscrepancyNoteService() {
        discrepancyNoteService = this.discrepancyNoteService != null ? discrepancyNoteService : new DiscrepancyNoteService(ds);
        return discrepancyNoteService;
    }

}
