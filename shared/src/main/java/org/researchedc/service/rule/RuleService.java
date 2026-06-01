/* 
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * OpenClinica is distributed under the
 * Copyright 2003-2008 Akaza Research 
 */
package org.researchedc.service.rule;

import org.researchedc.bean.oid.GenericOidGenerator;
import org.researchedc.bean.oid.OidGenerator;
import org.researchedc.bean.rule.RuleBean;
import org.researchedc.bean.rule.RuleSetBean;
import org.researchedc.dao.spi.IRuleDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service("legacyRuleService")
public class RuleService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    DataSource ds;
    private IRuleDAO ruleDao;
    private OidGenerator oidGenerator;

    public RuleService(DataSource ds) {
        oidGenerator = new GenericOidGenerator();
        this.ds = ds;
    }

    @Autowired
    public RuleService(DataSource ds, IRuleDAO ruleDao) {
        this(ds);
        this.ruleDao = ruleDao;
    }

    public boolean enableRules(RuleSetBean ruleSet) {
        return true;
    }

    public boolean disableRules() {
        return true;

    }

    public RuleBean saveRule(RuleBean ruleBean) {
        return (RuleBean) getRuleDao().create(ruleBean);
    }

    public RuleBean updateRule(RuleBean ruleBean) {
        return (RuleBean) getRuleDao().update(ruleBean);
    }

    private IRuleDAO getRuleDao() {
        return ruleDao;
    }

}
