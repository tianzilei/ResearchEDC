package org.researchedc.domain.rule.action;

import org.researchedc.dao.spi.RuleActionRunLogDomainDao;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.RuleSetRuleBean;
import org.researchedc.exception.OpenClinicaSystemException;
import org.researchedc.service.crfdata.DynamicsMetadataService;

import javax.sql.DataSource;

public class ActionProcessorFacade {

    public static ActionProcessor getActionProcessor(ActionType actionType, DataSource ds,
            DynamicsMetadataService itemMetadataService, RuleSetBean ruleSet, RuleActionRunLogDomainDao ruleActionRunLogDao, RuleSetRuleBean ruleSetRule,
            IStudyDAO studyDao, IStudySubjectDAO studySubjectDao, IStudyEventDAO studyEventDao,
            IStudyEventDefinitionDAO studyEventDefinitionDao, IStudyParameterValueDAO studyParameterValueDao, IUserAccountDAO userAccountDao,
            IDiscrepancyNoteDAO discrepancyNoteDao)
            throws OpenClinicaSystemException {
        switch (actionType) {
        case FILE_DISCREPANCY_NOTE:
            return new DiscrepancyNoteActionProcessor(ds, ruleActionRunLogDao, ruleSetRule, discrepancyNoteDao);
        case SHOW:
            return new ShowActionProcessor(ds, itemMetadataService, ruleSet);
        case HIDE:
            return new HideActionProcessor(ds, itemMetadataService, ruleSet);
        case INSERT:
            return new InsertActionProcessor(ds, itemMetadataService, ruleActionRunLogDao, ruleSet, ruleSetRule);
        case RANDOMIZE:
            return new RandomizeActionProcessor(ds, itemMetadataService, ruleActionRunLogDao, ruleSet, ruleSetRule, studyDao, studyParameterValueDao);
        default:
            throw new OpenClinicaSystemException("actionType", "Unrecognized action type!");
        }
    }
}
