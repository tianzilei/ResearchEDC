/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.researchedc.dao.rule;
import org.researchedc.dao.spi.IRuleDAO;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.rule.RuleBean;
import org.researchedc.bean.rule.RuleSetBean;
import org.researchedc.bean.rule.expression.ExpressionBean;
import org.researchedc.dao.core.AuditableEntityDAO;
import org.researchedc.dao.core.DAODigester;
import org.researchedc.dao.core.SQLFactory;
import org.researchedc.dao.core.TypeNames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

import javax.sql.DataSource;

/**
 * <p>
 * Manage Rules
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class RuleDAO extends AuditableEntityDAO implements IRuleDAO {

    private ExpressionDAO expressionDao;
    private Function<DataSource, ExpressionDAO> expressionDaoFactory = ExpressionDAO::new;

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public RuleDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    private ExpressionDAO getExpressionDao() {
        if (expressionDao == null) {
            expressionDao = expressionDaoFactory.apply(ds);
        }
        return expressionDao;
    }

    public RuleDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULE;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // rule_id
        this.setTypeExpected(2, TypeNames.STRING); // name
        this.setTypeExpected(3, TypeNames.STRING); // description
        this.setTypeExpected(4, TypeNames.STRING); // oc_oid
        this.setTypeExpected(5, TypeNames.BOOL); // enabled
        this.setTypeExpected(6, TypeNames.INT); // expression_id

        // Standard set of fields
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
        variables.put(Integer.valueOf(2), ruleBean.getDescription());
        variables.put(Integer.valueOf(3), ruleBean.getUpdaterId());
        variables.put(Integer.valueOf(4), ruleBean.getId());
        getExpressionDao().update(ruleBean.getExpression());

        this.execute(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            ruleBean.setActive(true);
        }

        return ruleBean;
    }

    public EntityBean create(EntityBean eb) {
        RuleBean ruleBean = (RuleBean) eb;
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap<Integer, Object> nullVars = new HashMap<Integer, Object>();

        variables.put(Integer.valueOf(1), ruleBean.getName());
        variables.put(Integer.valueOf(2), ruleBean.getDescription());
        variables.put(Integer.valueOf(3), ruleBean.getOid());
        variables.put(Integer.valueOf(4), ruleBean.isEnabled());
        variables.put(Integer.valueOf(5), getExpressionDao().create(ruleBean.getExpression()).getId());

        variables.put(Integer.valueOf(6), Integer.valueOf(ruleBean.getOwnerId()));
        variables.put(Integer.valueOf(7), Integer.valueOf(Status.AVAILABLE.getId()));

        executeWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            ruleBean.setId(getLatestPK());
        }

        return ruleBean;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        RuleBean ruleBean = new RuleBean();
        this.setEntityAuditInformation(ruleBean, hm);

        ruleBean.setId(((Integer) hm.get("rule_id")).intValue());
        ruleBean.setName(((String) hm.get("name")));
        ruleBean.setOid(((String) hm.get("oc_oid")));
        ruleBean.setEnabled(((Boolean) hm.get("enabled")));
        int expressionId = ((Integer) hm.get("rule_expression_id")).intValue();
        ruleBean.setExpression((ExpressionBean) getExpressionDao().findByPK(expressionId));

        return ruleBean;
    }

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public EntityBean findByPK(int ID) {
        RuleBean ruleBean = new RuleBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), Integer.valueOf(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            ruleBean = (RuleBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return ruleBean;
    }

    public RuleBean findByOid(RuleBean ruleBean) {
        RuleBean ruleBeanInDb = new RuleBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), new String(ruleBean.getOid()));

        String sql = digester.getQuery("findByOid");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        if (it.hasNext()) {
            ruleBeanInDb = (RuleBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
        }
        if (alist.isEmpty()) {
            ruleBeanInDb = null;
        }
        return ruleBeanInDb;
    }

    public RuleBean findByOid(String oid) {
        RuleBean ruleBeanInDb = new RuleBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), new String(oid));

        String sql = digester.getQuery("findByOid");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        if (it.hasNext()) {
            ruleBeanInDb = (RuleBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
        }
        if (alist.isEmpty()) {
            ruleBeanInDb = null;
        }
        return ruleBeanInDb;
    }

    public ArrayList<RuleBean> findByRuleSet(RuleSetBean ruleSet) {
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        Integer eventCrfBeanId = Integer.valueOf(ruleSet.getId());
        variables.put(Integer.valueOf(1), eventCrfBeanId);

        String sql = digester.getQuery("findByRuleSet");
        ArrayList alist = this.select(sql, variables);
        ArrayList<RuleBean> ruleSetBeans = new ArrayList<RuleBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            RuleBean ruleBean = (RuleBean) this.getEntityFromHashMap((HashMap) it.next());
            ruleSetBeans.add(ruleBean);
        }
        return ruleSetBeans;
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
