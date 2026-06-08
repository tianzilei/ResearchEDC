package org.researchedc.logic.rulerunner;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.domain.rule.RuleBean;
import org.researchedc.domain.rule.RuleSetBasedViewContainer;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.RuleSetRuleBean;
import org.researchedc.domain.rule.action.ActionProcessor;
import org.researchedc.domain.rule.action.ActionProcessorFacade;
import org.researchedc.domain.rule.action.RuleActionBean;
import org.researchedc.domain.rule.action.RuleActionRunBean.Phase;
import org.researchedc.domain.rule.action.RuleActionRunLogBean;
import org.researchedc.domain.rule.expression.ExpressionBean;
import org.researchedc.domain.rule.expression.ExpressionObjectWrapper;
import org.researchedc.exception.OpenClinicaSystemException;
import org.researchedc.logic.expressionTree.OpenClinicaExpressionParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public class RuleSetBulkRuleRunner extends RuleRunner {

    public RuleSetBulkRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, Object mailSender) {
        super(ds, requestURLMinusServletPath, contextPath, mailSender);
    }

    private List<RuleSetBasedViewContainer> populateForRuleSetBasedView(List<RuleSetBasedViewContainer> theList, RuleSetBean ruleSet, RuleBean rule,
            String akey, RuleActionBean ruleAction) {

        StudyEventBean studyEvent =
            (StudyEventBean) getStudyEventDao().findByPK(
                    Integer.valueOf(getExpressionService().getStudyEventDefenitionOrdninalCurated(ruleSet.getTarget().getValue())));

        // for (String akey : actionsToBeExecuted.keySet()) {
        // for (RuleActionBean ruleAction : actionsToBeExecuted.get(akey)) {
        RuleSetBasedViewContainer container =
            new RuleSetBasedViewContainer(rule.getName(), rule.getOid(), rule.getExpression().getValue(), akey, ruleAction.getActionType().toString(),
                    ruleAction.getSummary());

        if (!theList.contains(container)) {
            theList.add(container);
        }

        StudySubjectBean studySubject = (StudySubjectBean) getStudySubjectDao().findByPK(studyEvent.getStudySubjectId());
        theList.get(theList.indexOf(container)).addSubject(studySubject.getLabel());
        // }
        // }
        return theList;

    }

    public List<RuleSetBasedViewContainer> runRulesBulkFromRuleSetScreenOLD(List<RuleSetBean> ruleSets, Boolean dryRun, StudyBean currentStudy,
            HashMap<String, String> variableAndValue, UserAccountBean ub) {

        if (variableAndValue == null || variableAndValue.isEmpty()) {
            logger.warn("You must be executing Rules in Batch");
            variableAndValue = new HashMap<String, String>();
        }

        List<RuleSetBasedViewContainer> ruleSetBasedView = new ArrayList<RuleSetBasedViewContainer>();
        for (RuleSetBean ruleSet : ruleSets) {
            for (ExpressionBean expressionBean : ruleSet.getExpressions()) {
                ruleSet.setTarget(expressionBean);

                for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) {
                    String result = null;
                    RuleBean rule = ruleSetRule.getRuleBean();
                    ExpressionObjectWrapper eow = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet, variableAndValue);
                    try {
                        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(eow);
                        result = (String) oep.parseAndEvaluateExpression(rule.getExpression().getValue());

                        // HashMap<String, ArrayList<RuleActionBean>> actionsToBeExecuted = ruleSetRule.getAllActionsWithEvaluatesToAsKey(result);
                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result, Phase.BATCH);

                        ItemDataBean itemData = getExpressionService().getItemDataBeanFromDb(ruleSet.getTarget().getValue());
                        if (itemData != null) {
                            Iterator<RuleActionBean> itr = actionListBasedOnRuleExecutionResult.iterator();
                            while (itr.hasNext()) {
                                RuleActionBean ruleActionBean = itr.next();
                                RuleActionRunLogBean ruleActionRunLog =
                                    new RuleActionRunLogBean(ruleActionBean.getActionType(), itemData, itemData.getValue(), ruleSetRule.getRuleBean().getOid());
                                if (getRuleActionRunLogDao().findCountByRuleActionRunLogBean(ruleActionRunLog) > 0) {
                                    itr.remove();
                                }
                            }
                        }

                        logger.info("RuleSet with target  : {} , Ran Rule : {}  The Result was : {} , Based on that {} action will be executed ", new Object[] {
                            ruleSet.getTarget().getValue(), rule.getName(), result, actionListBasedOnRuleExecutionResult.size() });

                        if (actionListBasedOnRuleExecutionResult.size() > 0) {
                            for (RuleActionBean ruleAction : actionListBasedOnRuleExecutionResult) {
                                ruleAction.setCuratedMessage(curateMessage(ruleAction, ruleSetRule));
                                // getDiscrepancyNoteService().saveFieldNotes(ruleAction.getSummary(), itemDataBeanId, "ItemData", currentStudy, ub);
                                ActionProcessor ap =
                                    ActionProcessorFacade.getActionProcessor(ruleAction.getActionType(), ds, dynamicsMetadataService, ruleSet,
                                            getRuleActionRunLogDao(), ruleSetRule, getStudyDao(), getStudySubjectDao(), getStudyEventDao(),
                                            getStudyEventDefinitionDao(), getStudyParameterValueDao(), getUserAccountDao(), getDiscrepancyNoteDao());
                                RuleActionBean rab =
                                    ap.execute(RuleRunnerMode.RULSET_BULK, ExecutionMode.SAVE, ruleAction, itemData, DiscrepancyNoteBean.ITEM_DATA,
                                            currentStudy, ub, prepareEmailContents(ruleSet, ruleSetRule, currentStudy, ruleAction));
                                if (rab != null) {
                                    ruleSetBasedView = populateForRuleSetBasedView(ruleSetBasedView, ruleSet, rule, result, ruleAction);
                                }
                            }
                        }

                    } catch (OpenClinicaSystemException osa) {
                        String errorMessage =
                            "RuleSet with target  : " + ruleSet.getTarget().getValue() + " , Ran Rule : " + rule.getName()
                                + " , It resulted in an error due to : " + osa.getMessage();
                        // log error
                        logger.warn(errorMessage);

                    }
                }
            }
        }
        return ruleSetBasedView;
    }

    public List<RuleSetBasedViewContainer> runRulesBulkFromRuleSetScreen(List<RuleSetBean> ruleSets, ExecutionMode executionMode, StudyBean currentStudy,
            HashMap<String, String> variableAndValue, UserAccountBean ub) {

        if (variableAndValue == null || variableAndValue.isEmpty()) {
            logger.warn("You must be executing Rules in Batch");
            variableAndValue = new HashMap<String, String>();
        }

        List<RuleSetBasedViewContainer> ruleSetBasedView = new ArrayList<RuleSetBasedViewContainer>();
        HashMap<String, ArrayList<RuleActionContainer>> toBeExecuted = new HashMap<String, ArrayList<RuleActionContainer>>();
        for (RuleSetBean ruleSet : ruleSets) {
            String key = getExpressionService().getItemOid(ruleSet.getOriginalTarget().getValue());
            List<RuleActionContainer> allActionContainerListBasedOnRuleExecutionResult = null;
            if (toBeExecuted.containsKey(key)) {
                allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
            } else {
                toBeExecuted.put(key, new ArrayList<RuleActionContainer>());
                allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
            }
            ItemDataBean itemData = null;

            for (ExpressionBean expressionBean : ruleSet.getExpressions()) {
                ruleSet.setTarget(expressionBean);
				System.out.println("tg expression:" + ruleSet.getTarget().getValue());

                for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) {
                    String result = null;
                    RuleBean rule = ruleSetRule.getRuleBean();
                    ExpressionObjectWrapper eow = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet, variableAndValue);
                    try {
                        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(eow);
                        result = (String) oep.parseAndEvaluateExpression(rule.getExpression().getValue());
                        itemData = getExpressionService().getItemDataBeanFromDb(ruleSet.getTarget().getValue());
						System.out.println("The result: " + result);

                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result, Phase.BATCH);

                        if (itemData != null) {
                            Iterator<RuleActionBean> itr = actionListBasedOnRuleExecutionResult.iterator();
                            while (itr.hasNext()) {
                                RuleActionBean ruleActionBean = itr.next();
                                RuleActionRunLogBean ruleActionRunLog =
                                    new RuleActionRunLogBean(ruleActionBean.getActionType(), itemData, itemData.getValue(), ruleSetRule.getRuleBean().getOid());
                                if (getRuleActionRunLogDao().findCountByRuleActionRunLogBean(ruleActionRunLog) > 0) {
                                    itr.remove();
                                }
                            }
                        }
                        for (RuleActionBean ruleActionBean : actionListBasedOnRuleExecutionResult) {
                            RuleActionContainer ruleActionContainer = new RuleActionContainer(ruleActionBean, expressionBean, itemData, ruleSet);
                            allActionContainerListBasedOnRuleExecutionResult.add(ruleActionContainer);
                        }
                        logger.info("RuleSet with target  : {} , Ran Rule : {}  The Result was : {} , Based on that {} action will be executed in {} mode. ",
                                new Object[] { ruleSet.getTarget().getValue(), rule.getName(), result, actionListBasedOnRuleExecutionResult.size(),
                                    executionMode.name() });
                    } catch (OpenClinicaSystemException osa) {
                        // TODO: report something useful
                    }
                }
            }
        }

        for (Map.Entry<String, ArrayList<RuleActionContainer>> entry : toBeExecuted.entrySet()) {
            // Sort the list of actions
            Collections.sort(entry.getValue(), new RuleActionContainerComparator());

            for (RuleActionContainer ruleActionContainer : entry.getValue()) {
                ruleActionContainer.getRuleSetBean().setTarget(ruleActionContainer.getExpressionBean());
                ruleActionContainer.getRuleAction().setCuratedMessage(
                        curateMessage(ruleActionContainer.getRuleAction(), ruleActionContainer.getRuleAction().getRuleSetRule()));
                ActionProcessor ap =
                    ActionProcessorFacade.getActionProcessor(ruleActionContainer.getRuleAction().getActionType(), ds, dynamicsMetadataService,
                            ruleActionContainer.getRuleSetBean(), getRuleActionRunLogDao(), ruleActionContainer.getRuleAction().getRuleSetRule(),
                            getStudyDao(), getStudySubjectDao(), getStudyEventDao(), getStudyEventDefinitionDao(), getStudyParameterValueDao(),
                            getUserAccountDao(), getDiscrepancyNoteDao());
                RuleActionBean rab =
                    ap.execute(
                            RuleRunnerMode.RULSET_BULK,
                            executionMode,
                            ruleActionContainer.getRuleAction(),
                            ruleActionContainer.getItemDataBean(),
                            DiscrepancyNoteBean.ITEM_DATA,
                            currentStudy,
                            ub,
                            prepareEmailContents(ruleActionContainer.getRuleSetBean(), ruleActionContainer.getRuleAction().getRuleSetRule(), currentStudy,
                                    ruleActionContainer.getRuleAction()));
				System.out.println(" Action Trigger: " + ap.toString());

                if (rab != null) {
                    ruleSetBasedView =
                        populateForRuleSetBasedView(ruleSetBasedView, ruleActionContainer.getRuleSetBean(), ruleActionContainer.getRuleAction()
                                .getRuleSetRule().getRuleBean(), ruleActionContainer.getRuleAction().getExpressionEvaluatesTo().toString(),
                                ruleActionContainer.getRuleAction());
                }
            }

        }
        return ruleSetBasedView;
    }

}
