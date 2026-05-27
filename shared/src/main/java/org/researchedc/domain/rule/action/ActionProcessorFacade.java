package org.researchedc.domain.rule.action;

import org.researchedc.dao.hibernate.RuleActionRunLogDao;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.managestudy.StudySubjectDAO;
import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.RuleSetRuleBean;
import org.researchedc.exception.OpenClinicaSystemException;
import org.researchedc.service.crfdata.DynamicsMetadataService;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.sql.DataSource;

public class ActionProcessorFacade {

    public static ActionProcessor getActionProcessor(ActionType actionType, DataSource ds, JavaMailSenderImpl mailSender,
            DynamicsMetadataService itemMetadataService, RuleSetBean ruleSet, RuleActionRunLogDao ruleActionRunLogDao, RuleSetRuleBean ruleSetRule,
            StudyDAO studyDao, StudySubjectDAO studySubjectDao, StudyEventDAO studyEventDao,
            StudyEventDefinitionDAO studyEventDefinitionDao, StudyParameterValueDAO studyParameterValueDao, UserAccountDAO userAccountDao)
            throws OpenClinicaSystemException {
        switch (actionType) {
        case FILE_DISCREPANCY_NOTE:
            return new DiscrepancyNoteActionProcessor(ds, ruleActionRunLogDao, ruleSetRule);
        case EMAIL:
            return new EmailActionProcessor(ds, mailSender, ruleActionRunLogDao, ruleSetRule);
        case NOTIFICATION:
            return new NotificationActionProcessor(ds, mailSender, ruleSetRule, studySubjectDao, userAccountDao, studyParameterValueDao, studyDao, studyEventDao,
                    studyEventDefinitionDao);
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
