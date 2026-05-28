/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.researchedc.dao.rule.action;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.rule.RuleBean;
import org.researchedc.bean.rule.RuleSetRuleBean;
import org.researchedc.bean.rule.action.ActionType;
import org.researchedc.bean.rule.action.DiscrepancyNoteActionBean;
import org.researchedc.bean.rule.action.EmailActionBean;
import org.researchedc.bean.rule.action.RuleActionBean;
import org.researchedc.dao.core.AuditableEntityDAO;
import org.researchedc.dao.core.DAODigester;
import org.researchedc.dao.core.SQLFactory;
import org.researchedc.dao.core.TypeNames;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.rule.RuleDAO;
import org.researchedc.dao.rule.RuleSetDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.submit.ItemDataDAO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

import javax.sql.DataSource;

/**
 * <p>
 * Manage Actions
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class RuleActionDAO extends AuditableEntityDAO {

    private EventCRFDAO eventCrfDao;
    private RuleSetDAO ruleSetDao;
    private RuleDAO ruleDao;
    private ItemDataDAO itemDataDao;
    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private CRFVersionDAO crfVersionDao;
    private Function<DataSource, StudyEventDefinitionDAO> studyEventDefinitionDaoFactory = StudyEventDefinitionDAO::new;
    private Function<DataSource, RuleSetDAO> ruleSetDaoFactory = RuleSetDAO::new;
    private Function<DataSource, RuleDAO> ruleDaoFactory = RuleDAO::new;
    private Function<DataSource, EventCRFDAO> eventCrfDaoFactory = EventCRFDAO::new;
    private Function<DataSource, CRFVersionDAO> crfVersionDaoFactory = CRFVersionDAO::new;
    private Function<DataSource, ItemDataDAO> itemDataDaoFactory = ItemDataDAO::new;

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public RuleActionDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    private StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        if (studyEventDefinitionDao == null) {
            studyEventDefinitionDao = studyEventDefinitionDaoFactory.apply(ds);
        }
        return studyEventDefinitionDao;
    }

    private RuleSetDAO getRuleSetDao() {
        if (ruleSetDao == null) {
            ruleSetDao = ruleSetDaoFactory.apply(ds);
        }
        return ruleSetDao;
    }

    private RuleDAO getRuleDao() {
        if (ruleDao == null) {
            ruleDao = ruleDaoFactory.apply(ds);
        }
        return ruleDao;
    }

    private EventCRFDAO getEventCrfDao() {
        if (eventCrfDao == null) {
            eventCrfDao = eventCrfDaoFactory.apply(ds);
        }
        return eventCrfDao;
    }

    private CRFVersionDAO getCrfVersionDao() {
        if (crfVersionDao == null) {
            crfVersionDao = crfVersionDaoFactory.apply(ds);
        }
        return crfVersionDao;
    }

    private ItemDataDAO getItemDataDao() {
        if (itemDataDao == null) {
            itemDataDao = itemDataDaoFactory.apply(ds);
        }
        return itemDataDao;
    }

    public RuleActionDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULE_ACTION;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // rule_action_id
        this.setTypeExpected(2, TypeNames.INT); // rule_set_rule_id
        this.setTypeExpected(3, TypeNames.INT); // action_type
        this.setTypeExpected(4, TypeNames.BOOL); // expression_evaluates_to
        this.setTypeExpected(5, TypeNames.STRING); // message
        this.setTypeExpected(6, TypeNames.STRING); // email_to

        this.setTypeExpected(7, TypeNames.INT);// owner_id
        this.setTypeExpected(8, TypeNames.DATE); // date_created
        this.setTypeExpected(9, TypeNames.DATE);// date_updated
        this.setTypeExpected(10, TypeNames.INT);// updater_id
        this.setTypeExpected(11, TypeNames.INT);// status_id

    }

    public EntityBean update(EntityBean eb) {
        RuleBean ruleBean = (RuleBean) eb;

        ruleBean.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap nullVars = new HashMap();
        variables.put(Integer.valueOf(1), ruleBean.getName());

        this.execute(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            ruleBean.setActive(true);
        }

        return ruleBean;
    }

    public EntityBean create(EntityBean eb) {
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap<Integer, Object> nullVars = new HashMap<Integer, Object>();

        RuleActionBean ruleAction = null;

        if (eb instanceof DiscrepancyNoteActionBean) {
            DiscrepancyNoteActionBean dnActionBean = (DiscrepancyNoteActionBean) eb;
            Boolean expressionEvaluates = dnActionBean.getExpressionEvaluatesTo() == null ? true : dnActionBean.getExpressionEvaluatesTo();
            variables.put(Integer.valueOf(1), Integer.valueOf(dnActionBean.getRuleSetRule().getId()));
            variables.put(Integer.valueOf(2), dnActionBean.getActionType().getCode());
            variables.put(Integer.valueOf(3), expressionEvaluates);
            variables.put(Integer.valueOf(4), dnActionBean.getMessage());

            variables.put(Integer.valueOf(5), Integer.valueOf(dnActionBean.getOwnerId()));
            variables.put(Integer.valueOf(6), Integer.valueOf(Status.AVAILABLE.getId()));

            executeWithPK(digester.getQuery("create_dn"), variables, nullVars);
            if (isQuerySuccessful()) {
                dnActionBean.setId(getLatestPK());
            }

            ruleAction = dnActionBean;
        }

        if (eb instanceof EmailActionBean) {
            EmailActionBean emailActionBean = (EmailActionBean) eb;
            Boolean expressionEvaluates = emailActionBean.getExpressionEvaluatesTo() == null ? true : emailActionBean.getExpressionEvaluatesTo();
            variables.put(Integer.valueOf(1), Integer.valueOf(emailActionBean.getRuleSetRule().getId()));
            variables.put(Integer.valueOf(2), emailActionBean.getActionType().getCode());
            variables.put(Integer.valueOf(3), expressionEvaluates);
            variables.put(Integer.valueOf(4), emailActionBean.getMessage());
            variables.put(Integer.valueOf(5), emailActionBean.getTo());

            variables.put(Integer.valueOf(6), Integer.valueOf(emailActionBean.getOwnerId()));
            variables.put(Integer.valueOf(7), Integer.valueOf(Status.AVAILABLE.getId()));

            executeWithPK(digester.getQuery("create_email"), variables, nullVars);
            if (isQuerySuccessful()) {
                emailActionBean.setId(getLatestPK());
            }

            ruleAction = emailActionBean;
        }

        return ruleAction;
    }

    public RuleActionBean getEntityFromHashMap(HashMap hm) {

        int actionTypeId = ((Integer) hm.get("action_type")).intValue();
        ActionType actionType = ActionType.getByCode(actionTypeId);
        RuleActionBean ruleAction = null;

        switch (actionType) {
        case FILE_DISCREPANCY_NOTE:
            ruleAction = new DiscrepancyNoteActionBean();
            ((DiscrepancyNoteActionBean) ruleAction).setMessage(((String) hm.get("message")));
        case EMAIL:
            ruleAction = new EmailActionBean();
            ((EmailActionBean) ruleAction).setMessage(((String) hm.get("message")));
            ((EmailActionBean) ruleAction).setTo(((String) hm.get("email_to")));
        }

        this.setEntityAuditInformation(ruleAction, hm);
        ruleAction.setActionType(actionType);
        ruleAction.setId(((Integer) hm.get("rule_action_id")).intValue());
        ruleAction.setExpressionEvaluatesTo(((Boolean) hm.get("expression_evaluates_to")).booleanValue());

        return ruleAction;
    }

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList<RuleActionBean> ruleSetBeans = new ArrayList<RuleActionBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            RuleActionBean ruleSet = this.getEntityFromHashMap((HashMap) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public EntityBean findByPK(int ID) {
        RuleActionBean action = new RuleActionBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), Integer.valueOf(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            action = this.getEntityFromHashMap((HashMap) it.next());
        }

        return action;
    }

    public ArrayList<RuleActionBean> findByRuleSetRule(RuleSetRuleBean ruleSetRule) {
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        Integer ruleSetRuleId = Integer.valueOf(ruleSetRule.getId());
        variables.put(Integer.valueOf(1), ruleSetRuleId);

        String sql = digester.getQuery("findByRuleSetRule");
        ArrayList<?> alist = this.select(sql, variables);
        ArrayList<RuleActionBean> ruleActionBeans = new ArrayList<RuleActionBean>();
        Iterator<?> it = alist.iterator();
        while (it.hasNext()) {
            RuleActionBean ruleActionBean = this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleActionBean.setRuleSetRule(ruleSetRule);
            ruleActionBeans.add(ruleActionBean);
        }
        return ruleActionBeans;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

}
