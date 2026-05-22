/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.researchedc.control.submit;

import org.researchedc.bean.core.Role;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.core.SecureController;
import org.researchedc.dao.spi.IRuleSetDAO;
import org.researchedc.dao.hibernate.RuleSetRuleAuditDao;
import org.researchedc.dao.hibernate.RuleSetRuleDao;
import org.researchedc.domain.Status;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.RuleSetRuleAuditBean;
import org.researchedc.domain.rule.RuleSetRuleBean;
import org.researchedc.service.rule.RuleSetServiceInterface;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.researchedc.dao.spi.IRuleDAO;

/**
 * @author Krikor Krumlian
 *
 */
public class UpdateRuleSetRuleServlet extends SecureController {

    private static final long serialVersionUID = 1L;
    IRuleSetDAO ruleSetDao;
    RuleSetServiceInterface ruleSetService;
    RuleSetRuleDao ruleSetRuleDao;
    RuleSetRuleAuditDao ruleSetRuleAuditDao;

    private static String RULESET_ID = "ruleSetId";
    private static String RULESETRULE_ID = "ruleSetRuleId";
    private static String RULESET = "ruleSet";
    private static String ACTION = "action";

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String ruleSetId = request.getParameter(RULESET_ID);
        String ruleSetRuleId = request.getParameter(RULESETRULE_ID);
        String source = request.getParameter("source");
        String action = request.getParameter(ACTION);
        Status status = null;
        String pageMessage = "";
        if (ruleSetRuleId != null) {
            RuleSetRuleBean ruleSetRule = getRuleSetRuleDao().findById(Integer.valueOf(ruleSetRuleId));
            if (ruleSetRuleId != null && action.equals("remove")) {
                status = Status.DELETED;
                updateRuleSetRule(ruleSetRule, status);
                pageMessage = "view_rules_remove_confirmation";
            } else if (ruleSetRuleId != null && action.equals("restore")) {
                status = Status.AVAILABLE;
                ruleSetRule.getRuleSetBean().setStatus(Status.AVAILABLE);
                updateRuleSetRule(ruleSetRule, status);
                pageMessage = "view_rules_restore_confirmation";
            }
        }
        if (ruleSetRuleId == null && ruleSetId != null && action.equals("remove")) {
            RuleSetBean rs = getRuleSetDao().findById(Integer.valueOf(ruleSetId));
            for (RuleSetRuleBean theRuleSetRule : rs.getRuleSetRules()) {
                if (theRuleSetRule.getStatus() != Status.DELETED) {
                    status = Status.DELETED;
                    updateRuleSetRule(theRuleSetRule, status);
                }
          }
            pageMessage = "view_rules_remove_confirmation";
        }

        addPageMessage(resword.getString(pageMessage));
        if (source != null && source.equals("ViewRuleSet")) {

            context.getRequestDispatcher("/ViewRuleSet?ruleSetId=" + ruleSetId).forward(request, response);
        } else {
            forwardPage(Page.LIST_RULE_SETS_SERVLET);
        }
    }

    private void updateRuleSetRule(RuleSetRuleBean ruleSetRule, Status status) {
        ruleSetRule.setStatus(status);
        ruleSetRule.setUpdater(ub);
        ruleSetRule = getRuleSetRuleDao().saveOrUpdate(ruleSetRule);
        createRuleSetRuleAuditBean(ruleSetRule, ub, status);

    }

    private void createRuleSetRuleAuditBean(RuleSetRuleBean ruleSetRuleBean, UserAccountBean ub, Status status) {
        RuleSetRuleAuditBean ruleSetRuleAuditBean = new RuleSetRuleAuditBean();
        ruleSetRuleAuditBean.setRuleSetRuleBean(ruleSetRuleBean);
        ruleSetRuleAuditBean.setUpdater(ub);
        ruleSetRuleAuditBean.setStatus(status);
        getRuleSetRuleAuditDao().saveOrUpdate(ruleSetRuleAuditBean);
    }

    private IRuleSetDAO getRuleSetDao() {
        ruleSetDao = this.ruleSetDao != null ? ruleSetDao : (IRuleSetDAO) SpringServletAccess.getApplicationContext(context).getBean("ruleSetDao");
        return ruleSetDao;
    }

    private RuleSetRuleDao getRuleSetRuleDao() {
        ruleSetRuleDao =
            this.ruleSetRuleDao != null ? ruleSetRuleDao : (RuleSetRuleDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetRuleDao");
        return ruleSetRuleDao;
    }

    private RuleSetRuleAuditDao getRuleSetRuleAuditDao() {
        ruleSetRuleAuditDao =
            this.ruleSetRuleAuditDao != null ? ruleSetRuleAuditDao : (RuleSetRuleAuditDao) SpringServletAccess.getApplicationContext(context).getBean(
                    "ruleSetRuleAuditDao");
        return ruleSetRuleAuditDao;
    }
}
