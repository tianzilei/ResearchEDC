/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.researchedc.dao.rule;
import org.researchedc.dao.spi.IRuleSetDAO;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.rule.RuleSetBean;
import org.researchedc.bean.rule.expression.Context;
import org.researchedc.bean.rule.expression.ExpressionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.LegacyDaoFactory;
import org.researchedc.dao.core.AuditableEntityDAO;
import org.researchedc.dao.core.DAODigester;
import org.researchedc.dao.core.SQLFactory;
import org.researchedc.dao.core.TypeNames;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.service.rule.expression.ExpressionService;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

import javax.sql.DataSource;

/**
 * <p>
 * Manage RuleSets
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class RuleSetDAO extends AuditableEntityDAO implements IRuleSetDAO {

    private EventCRFDAO eventCrfDao;
    private IStudyEventDefinitionDAO studyEventDefinitionDAO;
    private RuleDAO ruleDao;
    private ExpressionDAO expressionDao;
    private ICrfDAO crfDao;
    private ICrfVersionDAO crfVersionDao;
    private ExpressionService expressionService;
    private RuleSetRuleDAO ruleSetRuleDao;
    private RuleSetAuditDAO ruleSetAuditDao;
    private Function<DataSource, IStudyEventDefinitionDAO> studyEventDefinitionDaoFactory = LegacyDaoFactory::studyEventDefinitionDao;
    private Function<DataSource, ICrfDAO> crfDaoFactory = LegacyDaoFactory::crfDao;
    private Function<DataSource, ICrfVersionDAO> crfVersionDaoFactory = LegacyDaoFactory::crfVersionDao;
    private Function<DataSource, EventCRFDAO> eventCrfDaoFactory = EventCRFDAO::new;
    private Function<DataSource, RuleDAO> ruleDaoFactory = RuleDAO::new;
    private Function<DataSource, RuleSetAuditDAO> ruleSetAuditDaoFactory = RuleSetAuditDAO::new;
    private Function<DataSource, ExpressionDAO> expressionDaoFactory = ExpressionDAO::new;
    private Function<DataSource, RuleSetRuleDAO> ruleSetRuleDaoFactory = RuleSetRuleDAO::new;

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public RuleSetDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    private IStudyEventDefinitionDAO getStudyEventDefinitionDao() {
        if (studyEventDefinitionDAO == null) {
            studyEventDefinitionDAO = studyEventDefinitionDaoFactory.apply(ds);
        }
        return studyEventDefinitionDAO;
    }

    private ICrfDAO getCrfDao() {
        if (crfDao == null) {
            crfDao = crfDaoFactory.apply(ds);
        }
        return crfDao;
    }

    private ICrfVersionDAO getCrfVersionDao() {
        if (crfVersionDao == null) {
            crfVersionDao = crfVersionDaoFactory.apply(ds);
        }
        return crfVersionDao;
    }

    private EventCRFDAO getEventCrfDao() {
        if (eventCrfDao == null) {
            eventCrfDao = eventCrfDaoFactory.apply(ds);
        }
        return eventCrfDao;
    }

    private RuleDAO getRuleDao() {
        if (ruleDao == null) {
            ruleDao = ruleDaoFactory.apply(ds);
        }
        return ruleDao;
    }

    private RuleSetAuditDAO getRuleSetAuditDao() {
        if (ruleSetAuditDao == null) {
            ruleSetAuditDao = ruleSetAuditDaoFactory.apply(ds);
        }
        return ruleSetAuditDao;
    }

    private ExpressionDAO getExpressionDao() {
        if (expressionDao == null) {
            expressionDao = expressionDaoFactory.apply(ds);
        }
        return expressionDao;
    }

    private ExpressionService getExpressionService() {
        if (expressionService == null) {
            expressionService = new ExpressionService(ds);
        }
        return expressionService;
    }

    private RuleSetRuleDAO getRuleSetRuleDao() {
        if (ruleSetRuleDao == null) {
            ruleSetRuleDao = ruleSetRuleDaoFactory.apply(ds);
        }
        return ruleSetRuleDao;
    }

    public RuleSetDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULESET;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // ruleset_id
        this.setTypeExpected(2, TypeNames.INT);// expression_id
        this.setTypeExpected(3, TypeNames.INT);// study_event_definition_id
        this.setTypeExpected(4, TypeNames.INT);// crf_id
        this.setTypeExpected(5, TypeNames.INT);// crf_version_id
        this.setTypeExpected(6, TypeNames.INT);// study_id
        this.setTypeExpected(7, TypeNames.INT);// owner_id
        this.setTypeExpected(8, TypeNames.DATE); // date_created
        this.setTypeExpected(9, TypeNames.DATE);// date_updated
        this.setTypeExpected(10, TypeNames.INT);// updater_id
        this.setTypeExpected(11, TypeNames.INT);// status_id

    }

    public EntityBean update(EntityBean eb) {
        RuleSetBean ruleSetBean = (RuleSetBean) eb;

        ruleSetBean.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap nullVars = new HashMap();

        this.execute(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            ruleSetBean.setActive(true);
        }

        return ruleSetBean;
    }

    public EntityBean remove(RuleSetBean ruleSetBean, UserAccountBean ub) {
        ruleSetBean.setActive(false);

        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();

        variables.put(Integer.valueOf(1), Integer.valueOf(ub.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(Status.DELETED.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(ruleSetBean.getId()));

        this.execute(digester.getQuery("removeOrRestore"), variables);

        if (isQuerySuccessful()) {
            ruleSetBean.setActive(true);
            getRuleSetRuleDao().autoRemoveByRuleSet(ruleSetBean, ub);
            ruleSetBean.setStatus(Status.DELETED);
            getRuleSetAuditDao().create(ruleSetBean, ub);

        }

        return ruleSetBean;
    }

    public EntityBean restore(RuleSetBean ruleSetBean, UserAccountBean ub) {
        ruleSetBean.setActive(false);

        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();

        variables.put(Integer.valueOf(1), Integer.valueOf(ub.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(Status.AVAILABLE.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(ruleSetBean.getId()));

        this.execute(digester.getQuery("removeOrRestore"), variables);

        if (isQuerySuccessful()) {
            ruleSetBean.setActive(true);
            getRuleSetRuleDao().autoRestoreByRuleSet(ruleSetBean, ub);
            ruleSetBean.setStatus(Status.AVAILABLE);
            getRuleSetAuditDao().create(ruleSetBean, ub);
        }

        return ruleSetBean;
    }

    /*
     * I am going to attempt to use this create method as we use the saveOrUpdate method in Hibernate.
     */
    public EntityBean create(EntityBean eb) {
        RuleSetBean ruleSetBean = (RuleSetBean) eb;
        if (eb.getId() == 0) {
            HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
            HashMap<Integer, Object> nullVars = new HashMap<Integer, Object>();
            variables.put(Integer.valueOf(1), getExpressionDao().create(ruleSetBean.getTarget()).getId());
            variables.put(Integer.valueOf(2), Integer.valueOf(ruleSetBean.getStudyEventDefinition().getId()));
            if (ruleSetBean.getCrf() == null) {
                nullVars.put(Integer.valueOf(3), Integer.valueOf(Types.INTEGER));
                variables.put(Integer.valueOf(3), null);
        } else {
                variables.put(Integer.valueOf(3), Integer.valueOf(ruleSetBean.getCrf().getId()));
            }
            if (ruleSetBean.getCrfVersion() == null) {
                nullVars.put(Integer.valueOf(4), Integer.valueOf(Types.INTEGER));
                variables.put(Integer.valueOf(4), null);
            } else {
                variables.put(Integer.valueOf(4), Integer.valueOf(ruleSetBean.getCrfVersion().getId()));
            }
            variables.put(Integer.valueOf(5), Integer.valueOf(ruleSetBean.getStudy().getId()));
            variables.put(Integer.valueOf(6), Integer.valueOf(ruleSetBean.getOwnerId()));
            variables.put(Integer.valueOf(7), Integer.valueOf(Status.AVAILABLE.getId()));

            executeWithPK(digester.getQuery("create"), variables, nullVars);
            if (isQuerySuccessful()) {
                ruleSetBean.setId(getLatestPK());
            }

        }
        return ruleSetBean;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        RuleSetBean ruleSetBean = new RuleSetBean();
        this.setEntityAuditInformation(ruleSetBean, hm);

        ruleSetBean.setId(((Integer) hm.get("rule_set_id")).intValue());
        int expressionId = ((Integer) hm.get("rule_expression_id")).intValue();
        ExpressionBean expression = (ExpressionBean) getExpressionDao().findByPK(expressionId);
        ruleSetBean.setTarget(expression);
        ruleSetBean.setOriginalTarget(expression);
        ruleSetBean.setItemGroup(getExpressionService().getItemGroupExpression(ruleSetBean.getTarget().getValue()));
        ruleSetBean.setItem(getExpressionService().getItemExpression(ruleSetBean.getTarget().getValue(), ruleSetBean.getItemGroup()));
        int studyEventDefenitionId = ((Integer) hm.get("study_event_definition_id")).intValue();
        ruleSetBean.setStudyEventDefinition((StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(studyEventDefenitionId));
        int crfId = ((Integer) hm.get("crf_id")).intValue();
        ruleSetBean.setCrf((CRFBean) getCrfDao().findByPK(crfId));
        if ((Integer) hm.get("crf_version_id") != 0) {
            int crfVersionId = ((Integer) hm.get("crf_version_id")).intValue();
            ruleSetBean.setCrfVersion((CRFVersionBean) getCrfVersionDao().findByPK(crfVersionId));
        } else {
            ruleSetBean.setCrfVersion(null);
        }

        return ruleSetBean;
    }

    public RuleSetBean findByExpression(RuleSetBean ruleSetBean) {
        RuleSetBean ruleSetBeanInDb = new RuleSetBean();
        Context c = ruleSetBean.getTarget().getContext() == null ? Context.OC_RULES_V1 : ruleSetBean.getTarget().getContext();
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), c.getCode());
        variables.put(Integer.valueOf(2), ruleSetBean.getTarget().getValue());

        String sql = digester.getQuery("findByExpression");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        if (it.hasNext()) {
            ruleSetBeanInDb = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
        }
        if (alist.isEmpty()) {
            ruleSetBeanInDb = null;
        }
        return ruleSetBeanInDb;
    }

    private int getStudyId(StudyBean currentStudy) {
        return currentStudy.getParentStudyId() != 0 ? currentStudy.getParentStudyId() : currentStudy.getId();
    }

    public ArrayList<RuleSetBean> findByCrf(CRFBean crfBean, StudyBean currentStudy) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), crfBean.getId());
        variables.put(Integer.valueOf(2), getStudyId(currentStudy));

        String sql = digester.getQuery("findByCrfId");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public ArrayList<RuleSetBean> findByCrfVersionStudyAndStudyEventDefinition(CRFVersionBean crfVersionBean, StudyBean currentStudy,
            StudyEventDefinitionBean sed) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), crfVersionBean.getId());
        variables.put(Integer.valueOf(2), getStudyId(currentStudy));
        variables.put(Integer.valueOf(3), sed.getId());

        String sql = digester.getQuery("findByCrfVersionStudyAndStudyEventDefinition");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public ArrayList<RuleSetBean> findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(CRFVersionBean crfVersion, CRFBean crfBean, StudyBean currentStudy,
            StudyEventDefinitionBean sed) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), getStudyId(currentStudy));
        variables.put(Integer.valueOf(2), sed.getId());
        variables.put(Integer.valueOf(3), crfVersion.getId());
        variables.put(Integer.valueOf(4), crfBean.getId());
        variables.put(Integer.valueOf(5), crfBean.getId());

        String sql = digester.getQuery("findByCrfVersionOrCrfStudyAndStudyEventDefinition");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public ArrayList<RuleSetBean> findByCrfStudyAndStudyEventDefinition(CRFBean crfBean, StudyBean currentStudy, StudyEventDefinitionBean sed) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), crfBean.getId());
        variables.put(Integer.valueOf(2), getStudyId(currentStudy));
        variables.put(Integer.valueOf(3), sed.getId());

        String sql = digester.getQuery("findByCrfStudyAndStudyEventDefinition");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    @Override
    public ArrayList<RuleSetBean> findAllByStudy(StudyBean currentStudy) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), getStudyId(currentStudy));

        String sql = digester.getQuery("findAllByStudy");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
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
        RuleSetBean ruleSetBean = null;
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), Integer.valueOf(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            ruleSetBean = (RuleSetBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return ruleSetBean;
    }

    public RuleSetBean findByStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition) {
        RuleSetBean ruleSetBean = null;
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        Integer studyEventDefinitionId = Integer.valueOf(studyEventDefinition.getId());
        variables.put(Integer.valueOf(1), studyEventDefinitionId);

        String sql = digester.getQuery("findByStudyEventDefinition");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            ruleSetBean = (RuleSetBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return ruleSetBean;
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
