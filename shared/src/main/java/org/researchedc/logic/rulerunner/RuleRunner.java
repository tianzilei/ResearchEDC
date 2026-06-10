package org.researchedc.logic.rulerunner;

import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.RuleActionRunLogDomainDao;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.researchedc.dao.spi.IRuleSetDAO;
import org.researchedc.dao.spi.ISectionDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.domain.rule.RuleBulkExecuteContainer;
import org.researchedc.domain.rule.RuleBulkExecuteContainerTwo;
import org.researchedc.domain.rule.RuleSetRuleBean;
import org.researchedc.domain.rule.action.RuleActionBean;
import org.researchedc.service.crfdata.DynamicsMetadataService;
import org.researchedc.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

import javax.sql.DataSource;

public class RuleRunner {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private IRuleSetDAO ruleSetDao;
    private ICrfDAO crfDao;
    private ICrfVersionDAO crfVersionDao;
    private IStudyEventDAO studyEventDao;
    private IItemDataDAO itemDataDao;
    private ExpressionService expressionService;
    private EventCRFDao eventCrfDao;
    private IStudySubjectDAO studySubjectDao;
    private IStudyDAO studyDao;
    private IStudyEventDefinitionDAO studyEventDefinitionDao;
    private IStudyParameterValueDAO studyParameterValueDao;
    private IUserAccountDAO userAccountDao;
    private IDiscrepancyNoteDAO discrepancyNoteDao;
    private IItemFormMetadataDAO itemFormMetadataDao;
    private ISectionDAO sectionDao;
    protected RuleRunnerMode ruleRunnerMode;
    protected DynamicsMetadataService dynamicsMetadataService;
    protected RuleActionRunLogDomainDao ruleActionRunLogDao;
    DataSource ds;

    String requestURLMinusServletPath;
    String contextPath;

    public enum RuleRunnerMode {
        DATA_ENTRY, CRF_BULK, RULSET_BULK, IMPORT_DATA ,RUN_ON_SCHEDULE
    };


    public RuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath) {
        this.ds = ds;
        this.requestURLMinusServletPath = requestURLMinusServletPath;
        this.contextPath = contextPath;
    }


    String curateMessage(RuleActionBean ruleAction, RuleSetRuleBean ruleSetRule) {

        String message = ruleAction.getSummary();
        String ruleOid = ruleSetRule.getRuleBean().getOid();
        return ruleOid + " " + message;
    }

    void logCrfViewSpecificOrderedObjects(HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> crfViewSpecificOrderedObjects) {
        for (RuleBulkExecuteContainer key1 : crfViewSpecificOrderedObjects.keySet()) {
            for (RuleBulkExecuteContainerTwo key2 : crfViewSpecificOrderedObjects.get(key1).keySet()) {
                String studySubjects = "";
                for (String studySubjectIds : crfViewSpecificOrderedObjects.get(key1).get(key2)) {
                    studySubjects += studySubjectIds + " : ";
                }
                logger.debug("key1 {} , key2 {} , studySubjectId {}", new Object[] { key1.toString(), key2.toString(), studySubjects });
            }
        }
    }

    ExpressionService getExpressionService() {
        return expressionService;
    }

    IRuleSetDAO getRuleSetDao() {
        return ruleSetDao;
    }

    ICrfDAO getCrfDao() {
        return crfDao;
    }

    IStudyEventDAO getStudyEventDao() {
        return studyEventDao;
    }

    IItemDataDAO getItemDataDao() {
        return itemDataDao;
    }

    EventCRFDao getEventCrfDao() {
        return eventCrfDao;
    }

    ICrfVersionDAO getCrfVersionDao() {
        return crfVersionDao;
    }

    IStudySubjectDAO getStudySubjectDao() {
        return studySubjectDao;
    }

    IItemFormMetadataDAO getItemFormMetadataDAO() {
        return itemFormMetadataDao;
    }

    ISectionDAO getSectionDAO() {
        return sectionDao;
    }

    IStudyDAO getStudyDao() {
        return studyDao;
    }

    IStudyEventDefinitionDAO getStudyEventDefinitionDao() {
        return studyEventDefinitionDao;
    }

    IStudyParameterValueDAO getStudyParameterValueDao() {
        return studyParameterValueDao;
    }

    IUserAccountDAO getUserAccountDao() {
        return userAccountDao;
    }

    IDiscrepancyNoteDAO getDiscrepancyNoteDao() {
        return discrepancyNoteDao;
    }

    public void setDaoCollaborators(IRuleSetDAO ruleSetDao, ICrfDAO crfDao,
            IStudyEventDAO studyEventDao, IItemDataDAO itemDataDao, EventCRFDao eventCrfDao, ICrfVersionDAO crfVersionDao,
            IStudySubjectDAO studySubjectDao, IItemFormMetadataDAO itemFormMetadataDao, ISectionDAO sectionDao, IStudyDAO studyDao,
            IStudyEventDefinitionDAO studyEventDefinitionDao, IStudyParameterValueDAO studyParameterValueDao, IUserAccountDAO userAccountDao,
            IDiscrepancyNoteDAO discrepancyNoteDao, ExpressionService expressionService) {
        this.ruleSetDao = ruleSetDao;
        this.crfDao = crfDao;
        this.studyEventDao = studyEventDao;
        this.itemDataDao = itemDataDao;
        this.eventCrfDao = eventCrfDao;
        this.crfVersionDao = crfVersionDao;
        this.studySubjectDao = studySubjectDao;
        this.itemFormMetadataDao = itemFormMetadataDao;
        this.sectionDao = sectionDao;
        this.studyDao = studyDao;
        this.studyEventDefinitionDao = studyEventDefinitionDao;
        this.studyParameterValueDao = studyParameterValueDao;
        this.userAccountDao = userAccountDao;
        this.discrepancyNoteDao = discrepancyNoteDao;
        this.expressionService = expressionService;
    }

    public DynamicsMetadataService getDynamicsMetadataService() {
        return dynamicsMetadataService;
    }

    public void setDynamicsMetadataService(DynamicsMetadataService dynamicsMetadataService) {
        this.dynamicsMetadataService = dynamicsMetadataService;
    }

    public RuleActionRunLogDomainDao getRuleActionRunLogDao() {
        return ruleActionRunLogDao;
    }

    public void setRuleActionRunLogDao(RuleActionRunLogDomainDao ruleActionRunLogDao) {
        this.ruleActionRunLogDao = ruleActionRunLogDao;
    }

    
}
