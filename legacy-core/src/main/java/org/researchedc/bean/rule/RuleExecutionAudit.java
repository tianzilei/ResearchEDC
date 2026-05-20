/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.researchedc.bean.rule;

import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.rule.action.RuleActionBean;

import java.util.List;

/**
 * <p>
 * RuleSetBean, the object that collects rules associated with study events.
 * </p>
 * 
 * @author Krikor Krumlian
 */
public class RuleExecutionAudit extends AuditableEntityBean {

    private RuleSetRuleBean ruleSetRule;
    private StudyEventBean studyEvent;
    private String executionStatus;
    private String result;
    private List<RuleActionBean> actions;

}
