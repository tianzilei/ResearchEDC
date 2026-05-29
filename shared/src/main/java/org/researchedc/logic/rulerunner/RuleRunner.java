package org.researchedc.logic.rulerunner;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.hibernate.RuleActionRunLogDao;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.rule.RuleSetDAO;
import org.researchedc.dao.rule.RuleSetRuleDAO;
import org.researchedc.dao.rule.action.RuleActionDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.domain.rule.RuleBulkExecuteContainer;
import org.researchedc.domain.rule.RuleBulkExecuteContainerTwo;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.RuleSetRuleBean;
import org.researchedc.domain.rule.action.RuleActionBean;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.service.crfdata.DynamicsMetadataService;
import org.researchedc.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.sql.DataSource;

public class RuleRunner {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private RuleSetDAO ruleSetDao;
    private RuleSetRuleDAO ruleSetRuleDao;
    private RuleActionDAO ruleActionDao;
    private ICrfDAO crfDao;
    private CRFVersionDAO crfVersionDao;
    private StudyEventDAO studyEventDao;
    private ItemDataDAO itemDataDao;
    private ExpressionService expressionService;
    private EventCRFDAO eventCrfDao;
    private IStudySubjectDAO studySubjectDao;
    private IStudyDAO studyDao;
    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private IStudyParameterValueDAO studyParameterValueDao;
    private IUserAccountDAO userAccountDao;
    private DiscrepancyNoteDAO discrepancyNoteDao;
    private ItemFormMetadataDAO itemFormMetadataDao;
    private SectionDAO sectionDao;
    private JavaMailSenderImpl mailSender;
    protected RuleRunnerMode ruleRunnerMode;
    protected DynamicsMetadataService dynamicsMetadataService;
    protected RuleActionRunLogDao ruleActionRunLogDao;
    DataSource ds;

    String requestURLMinusServletPath;
    String contextPath;

    public enum RuleRunnerMode {
        DATA_ENTRY, CRF_BULK, RULSET_BULK, IMPORT_DATA ,RUN_ON_SCHEDULE
    };


    public RuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, JavaMailSenderImpl mailSender) {
        this.ds = ds;
        this.requestURLMinusServletPath = requestURLMinusServletPath;
        this.contextPath = contextPath;
        this.mailSender = mailSender;
    }


    String curateMessage(RuleActionBean ruleAction, RuleSetRuleBean ruleSetRule) {

        String message = ruleAction.getSummary();
        String ruleOid = ruleSetRule.getRuleBean().getOid();
        return ruleOid + " " + message;
    }

    HashMap<String, String> prepareEmailContents(RuleSetBean ruleSet, RuleSetRuleBean ruleSetRule, StudyBean currentStudy, RuleActionBean ruleAction) {

        // get the Study Event
        StudyEventBean studyEvent =
            (StudyEventBean) getStudyEventDao().findByPK(
                    Integer.valueOf(getExpressionService().getStudyEventDefenitionOrdninalCurated(ruleSet.getTarget().getValue())));
        // get the Study Subject
        StudySubjectBean studySubject = (StudySubjectBean) getStudySubjectDao().findByPK(studyEvent.getStudySubjectId());
        // get Study/Site Associated with Subject
        StudyBean theStudy = (StudyBean) getStudyDao().findByPK(studySubject.getStudyId());
        String theStudyName, theSiteName = "";
        if (theStudy.getParentStudyId() > 0) {
            StudyBean theParentStudy = (StudyBean) getStudyDao().findByPK(theStudy.getParentStudyId());
            theStudyName = theParentStudy.getName() + " / " + theParentStudy.getIdentifier();
            theSiteName = theStudy.getName() + " / " + theStudy.getIdentifier();
        } else {
            theStudyName = theStudy.getName() + " / " + theStudy.getIdentifier();
        }

        // get the eventCrf & subsequently the CRF Version
        //EventCRFBean eventCrf = (EventCRFBean) getEventCrfDao().findAllByStudyEvent(studyEvent).get(0);
        EventCRFBean eventCrf =
            (EventCRFBean) getEventCrfDao().findAllByStudyEventAndCrfOrCrfVersionOid(studyEvent,
                    getExpressionService().getCrfOid(ruleSet.getTarget().getValue())).get(0);

        CRFVersionBean crfVersion = (CRFVersionBean) getCrfVersionDao().findByPK(eventCrf.getCRFVersionId());
        CRFBean crf = (CRFBean) getCrfDao().findByPK(crfVersion.getCrfId());

        String studyEventDefinitionName = getExpressionService().getStudyEventDefinitionFromExpression(ruleSet.getTarget().getValue(), currentStudy).getName();
        studyEventDefinitionName += " [" + studyEvent.getSampleOrdinal() + "]";

        String itemGroupName = getExpressionService().getItemGroupNameAndOrdinal(ruleSet.getTarget().getValue());
        ItemGroupBean itemGroupBean = getExpressionService().getItemGroupExpression(ruleSet.getTarget().getValue());
        ItemBean itemBean = getExpressionService().getItemExpression(ruleSet.getTarget().getValue(), itemGroupBean);
        String itemName = itemBean.getName();

        SectionBean section =
            (SectionBean) getSectionDAO().findByPK(getItemFormMetadataDAO().findByItemIdAndCRFVersionId(itemBean.getId(), crfVersion.getId()).getSectionId());

        StringBuffer sb = new StringBuffer();
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle();

        sb.append(respage.getString("email_header_1"));

        sb.append(" " + contextPath + " ");
        sb.append(respage.getString("email_header_2"));
        sb.append(" '" + currentStudy.getName() + "' ");
        sb.append(respage.getString("email_header_3"));
        sb.append(" \n\n ");

        sb.append(respage.getString("email_body_1") + " " + theStudyName + " \n ");
        sb.append(respage.getString("email_body_1_a") + " " + theSiteName + " \n ");
        sb.append(respage.getString("email_body_2") + " " + studySubject.getName() + " \n ");
        sb.append(respage.getString("email_body_3") + " " + studyEventDefinitionName + " \n ");
        sb.append(respage.getString("email_body_4") + " " + crf.getName() + " " + crfVersion.getName() + " \n ");
        sb.append(respage.getString("email_body_5") + " " + section.getTitle() + " \n ");
        sb.append(respage.getString("email_body_6") + " " + itemGroupName + " \n ");
        sb.append(respage.getString("email_body_7") + " " + itemName + " \n ");
        sb.append(respage.getString("email_body_8") + " " + ruleAction.getCuratedMessage() + " \n ");

        sb.append(" \n\n ");
        sb.append(respage.getString("email_body_9"));
        sb.append(" " + contextPath + " ");
        sb.append(respage.getString("email_body_10"));
        sb.append(" \n");

        requestURLMinusServletPath = requestURLMinusServletPath == null ? "" : requestURLMinusServletPath;

        sb.append(requestURLMinusServletPath + "/ViewSectionDataEntry?ecId=" + eventCrf.getId() + "&sectionId=" + section.getId() + "&tabId="
            + section.getOrdinal());
        // &eventId="+ studyEvent.getId());
        sb.append("\n\n");
        sb.append(respage.getString("email_footer"));

        String subject = contextPath + " - [" + currentStudy.getName() + "] ";
        String ruleSummary = ruleAction.getSummary() != null ? ruleAction.getSummary() : "";
        String message = ruleSummary.length() < 20 ? ruleSummary : ruleSummary.substring(0, 20) + " ... ";
        subject += message;

        HashMap<String, String> emailContents = new HashMap<String, String>();
        emailContents.put("body", sb.toString());
        emailContents.put("subject", subject);

        return emailContents;
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

    RuleSetDAO getRuleSetDao() {
        return ruleSetDao;
    }

    ICrfDAO getCrfDao() {
        return crfDao;
    }

    RuleSetRuleDAO getRuleSetRuleDao() {
        return ruleSetRuleDao;
    }

    RuleActionDAO getRuleActionDao() {
        return ruleActionDao;
    }

    StudyEventDAO getStudyEventDao() {
        return studyEventDao;
    }

    ItemDataDAO getItemDataDao() {
        return itemDataDao;
    }

    EventCRFDAO getEventCrfDao() {
        return eventCrfDao;
    }

    CRFVersionDAO getCrfVersionDao() {
        return crfVersionDao;
    }

    IStudySubjectDAO getStudySubjectDao() {
        return studySubjectDao;
    }

    ItemFormMetadataDAO getItemFormMetadataDAO() {
        return itemFormMetadataDao;
    }

    SectionDAO getSectionDAO() {
        return sectionDao;
    }

    IStudyDAO getStudyDao() {
        return studyDao;
    }

    StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        return studyEventDefinitionDao;
    }

    IStudyParameterValueDAO getStudyParameterValueDao() {
        return studyParameterValueDao;
    }

    IUserAccountDAO getUserAccountDao() {
        return userAccountDao;
    }

    DiscrepancyNoteDAO getDiscrepancyNoteDao() {
        return discrepancyNoteDao;
    }

    public void setDaoCollaborators(RuleSetDAO ruleSetDao, ICrfDAO crfDao, RuleSetRuleDAO ruleSetRuleDao, RuleActionDAO ruleActionDao,
            StudyEventDAO studyEventDao, ItemDataDAO itemDataDao, EventCRFDAO eventCrfDao, CRFVersionDAO crfVersionDao,
            IStudySubjectDAO studySubjectDao, ItemFormMetadataDAO itemFormMetadataDao, SectionDAO sectionDao, IStudyDAO studyDao,
            StudyEventDefinitionDAO studyEventDefinitionDao, IStudyParameterValueDAO studyParameterValueDao, IUserAccountDAO userAccountDao,
            DiscrepancyNoteDAO discrepancyNoteDao, ExpressionService expressionService) {
        this.ruleSetDao = ruleSetDao;
        this.crfDao = crfDao;
        this.ruleSetRuleDao = ruleSetRuleDao;
        this.ruleActionDao = ruleActionDao;
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

    public JavaMailSenderImpl getMailSender() {
        return mailSender;
    }

    public DynamicsMetadataService getDynamicsMetadataService() {
        return dynamicsMetadataService;
    }

    public void setDynamicsMetadataService(DynamicsMetadataService dynamicsMetadataService) {
        this.dynamicsMetadataService = dynamicsMetadataService;
    }

    public RuleActionRunLogDao getRuleActionRunLogDao() {
        return ruleActionRunLogDao;
    }

    public void setRuleActionRunLogDao(RuleActionRunLogDao ruleActionRunLogDao) {
        this.ruleActionRunLogDao = ruleActionRunLogDao;
    }

    
}
