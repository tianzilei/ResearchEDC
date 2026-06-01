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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Krikor Krumlian
 *
 */
public class RemoveRuleSetServlet extends SecureController {

    @Autowired
    IRuleSetDAO ruleSetDao;
    RuleSetServiceInterface ruleSetService;
    RuleSetRuleAuditDao ruleSetRuleAuditDao;
    RuleSetRuleDao ruleSetRuleDao;

    private static String RULESET_ID = "ruleSetId";
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
        String action = request.getParameter(ACTION);
        if (ruleSetId == null) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_view"));
            forwardPage(Page.CRF_LIST);
        } else {
            RuleSetBean ruleSetBean = null;
            ruleSetBean = getRuleSetService().getRuleSetById(currentStudy, ruleSetId);
            if (action != null && action.equals("confirm")) {
                request.setAttribute(RULESET, ruleSetBean);
                forwardPage(Page.REMOVE_RULE_SET);
            } else {
                for (RuleSetRuleBean ruleSetRuleBean : ruleSetBean.getRuleSetRules()) {
                    if (ruleSetRuleBean.getStatus() != Status.DELETED) {
                        ruleSetRuleBean.setStatus(Status.DELETED);
                        ruleSetRuleBean.setUpdater(ub);
                        ruleSetRuleBean = getRuleSetRuleDao().saveOrUpdate(ruleSetRuleBean);
                        createRuleSetRuleAuditBean(ruleSetRuleBean, ub, Status.DELETED);
                    }
                }
                forwardPage(Page.LIST_RULE_SETS_SERVLET);
            }
        }
    }

    private void createRuleSetRuleAuditBean(RuleSetRuleBean ruleSetRuleBean, UserAccountBean ub, Status status) {
        RuleSetRuleAuditBean ruleSetRuleAuditBean = new RuleSetRuleAuditBean();
        ruleSetRuleAuditBean.setRuleSetRuleBean(ruleSetRuleBean);
        ruleSetRuleAuditBean.setUpdater(ub);
        ruleSetRuleAuditBean.setStatus(status);
        getRuleSetRuleAuditDao().saveOrUpdate(ruleSetRuleAuditBean);
    }

    /**
     * @return the ruleSetDao
     */
    public IRuleSetDAO getRuleSetDao() {
        return ruleSetDao;
    }

    /**
     * @param ruleSetDao the ruleSetDao to set
     */
    public void setRuleSetDao(IRuleSetDAO ruleSetDao) {
        this.ruleSetDao = ruleSetDao;
    }

    private RuleSetRuleAuditDao getRuleSetRuleAuditDao() {
        ruleSetRuleAuditDao =
            this.ruleSetRuleAuditDao != null ? ruleSetRuleAuditDao : (RuleSetRuleAuditDao) SpringServletAccess.getApplicationContext(context).getBean(
                    "ruleSetRuleAuditDao");
        return ruleSetRuleAuditDao;
    }

    private RuleSetRuleDao getRuleSetRuleDao() {
        ruleSetRuleDao =
            this.ruleSetRuleDao != null ? ruleSetRuleDao : (RuleSetRuleDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetRuleDao");
        return ruleSetRuleDao;
    }

    private RuleSetServiceInterface getRuleSetService() {
        ruleSetService =
            this.ruleSetService != null ? ruleSetService : (RuleSetServiceInterface) SpringServletAccess.getApplicationContext(context).getBean(
                    "ruleSetService");
        // TODO: Add getRequestURLMinusServletPath(),getContextPath()
        return ruleSetService;
    }

}
