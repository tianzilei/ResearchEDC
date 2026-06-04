package org.researchedc.bean.rule;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Function;

import javax.sql.DataSource;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.core.SessionManager;
import org.researchedc.dao.LegacyDaoFactory;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.spi.IRuleDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author Krikor Krumlian
 */

public class RuleExecutionBusinessObject {

    private final SessionManager sm;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected StudyBean currentStudy;
    protected UserAccountBean ub;
    private IDiscrepancyNoteDAO discrepancyNoteDao;
    private EventCRFDao eventCrfDao;
    private IRuleDAO ruleDao;
    private Function<DataSource, IDiscrepancyNoteDAO> discrepancyNoteDaoFactory = LegacyDaoFactory::discrepancyNoteDao;
    private Function<DataSource, EventCRFDao> eventCrfDaoFactory = LegacyDaoFactory::eventCrfDao;
    private Function<DataSource, IRuleDAO> ruleDaoFactory = LegacyDaoFactory::ruleDao;

    public RuleExecutionBusinessObject(SessionManager sm, StudyBean currentStudy, UserAccountBean ub) {
        this.sm = sm;
        this.currentStudy = currentStudy;
        this.ub = ub;
    }

    public void runRule(int eventCrfId) {
        // int eventCrfId = 11;
        EventCRFBean eventCrfBean = getEventCRFBean(eventCrfId);
        RuleSetBean ruleSetBean = getRuleSetBean(eventCrfBean);
        ArrayList<RuleBean> rules = getRuleBeans(ruleSetBean);
        for (RuleBean rule : rules) {
            initializeRule(rule);
        }
    }

    public void initializeRule(RuleBean rule) {
        // source data
        // ItemDataBean sourceItemDataBean = rule.getSourceItemDataBean();
        ItemDataBean sourceItemDataBean = null;

        // target data
        // ItemDataBean targetItemDataBean = rule.getTargetItemDataBean();
        ItemDataBean targetItemDataBean = null;

        // fireRules on source & target
        // TODO KK FIX HERE
        boolean sourceResult = true;// fireRule(sourceItemDataBean,rule.getSourceItemValue(),sourceItemFormMetadataBean,rule.getSourceOperator());
        boolean targetResult = true;// fireRule(targetItemDataBean,rule.getTargetItemValue(),targetItemFormMetadataBean,rule.getTargetOperator());

        if (sourceResult && targetResult) {
            // We are good
        }
        if (sourceResult == true && targetResult == false) {
            // file a descrepancy Note
            createDiscrepancyNote(rule.toString(), targetItemDataBean, sourceItemDataBean);
        }

    }

    private void createDiscrepancyNote(String description, ItemDataBean targetItemDataBean, ItemDataBean sourceItemDataBean) {

        DiscrepancyNoteBean note = new DiscrepancyNoteBean();
        note.setDescription(description);
        note.setDetailedNotes("");
        note.setOwner(ub);
        note.setCreatedDate(new Date());
        note.setResolutionStatusId(1);
        note.setDiscrepancyNoteTypeId(1);
        // note.setParentDnId(parentId);
        // note.setField(field);
        note.setEntityId(targetItemDataBean.getId());
        note.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
        note.setColumn("value");
        note.setStudyId(currentStudy.getId());

        note = (DiscrepancyNoteBean) getDiscrepancyNoteDao().create(note);
        getDiscrepancyNoteDao().createMapping(note);

    }

    // These are dao mostly calls see how to reduce redundancy
    private EventCRFBean getEventCRFBean(int eventCrfBeanId) {
        return eventCrfBeanId > 0 ? (EventCRFBean) getEventCrfDao().findByPK(eventCrfBeanId) : null;
    }

    private RuleSetBean getRuleSetBean(EventCRFBean eventCrfBean) {
        return null;
    }

    private ArrayList<RuleBean> getRuleBeans(RuleSetBean ruleSet) {
        return ruleSet != null ? getRuleDao().findByRuleSet(ruleSet) : new ArrayList<RuleBean>();
    }

    private IDiscrepancyNoteDAO getDiscrepancyNoteDao() {
        if (discrepancyNoteDao == null) {
            discrepancyNoteDao = discrepancyNoteDaoFactory.apply(sm.getDataSource());
        }
        return discrepancyNoteDao;
    }

    private EventCRFDao getEventCrfDao() {
        if (eventCrfDao == null) {
            eventCrfDao = eventCrfDaoFactory.apply(sm.getDataSource());
        }
        return eventCrfDao;
    }

    private IRuleDAO getRuleDao() {
        if (ruleDao == null) {
            ruleDao = ruleDaoFactory.apply(sm.getDataSource());
        }
        return ruleDao;
    }

}
