package org.researchedc.logic.rulerunner;

import java.util.List;

import javax.sql.DataSource;

//import com.ecyrd.speed4j.StopWatch;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.domain.Status;
import org.researchedc.domain.datamap.StudyEvent;
import org.researchedc.domain.rule.RuleBean;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.RuleSetRuleBean;
import org.researchedc.domain.rule.action.EventActionBean;
import org.researchedc.domain.rule.action.RuleActionBean;
import org.researchedc.domain.rule.expression.ExpressionBean;
import org.researchedc.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.researchedc.domain.rule.expression.ExpressionObjectWrapper;
import org.researchedc.exception.OpenClinicaSystemException;
import org.researchedc.logic.expressionTree.OpenClinicaExpressionParser;
import org.researchedc.patterns.ocobserver.StudyEventChangeDetails;
import org.researchedc.service.crfdata.BeanPropertyService;
import org.researchedc.service.rule.expression.ExpressionService;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author jnyayapathi
 *
 */
public class BeanPropertyRuleRunner extends RuleRunner{

	public BeanPropertyRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath) {
		super(ds, requestURLMinusServletPath, contextPath);
		// TODO Auto-generated constructor stub
	}

	public void runRules(List<RuleSetBean> ruleSets, DataSource ds,
                         BeanPropertyService beanPropertyService, IStudyEventDAO studyEventDaoHib, IStudyEventDefinitionDAO studyEventDefDaoHib,
                         StudyEventChangeDetails changeDetails,Integer userId)
	{
        for (RuleSetBean ruleSet : ruleSets)
        {
            List<ExpressionBean> expressions = ruleSet.getExpressions();
            for (ExpressionBean expressionBean : expressions) {
                ruleSet.setTarget(expressionBean);

                StudyEvent studyEvent = studyEventDaoHib.findByStudyEventId(
                        Integer.valueOf(getExpressionService().getStudyEventDefenitionOrdninalCurated(ruleSet.getTarget().getValue())));

                int eventOrdinal = studyEvent.getSampleOrdinal();
                int studySubjectBeanId = studyEvent.getStudySubject().getStudySubjectId();

                List<RuleSetRuleBean> ruleSetRules = ruleSet.getRuleSetRules();
                for (RuleSetRuleBean ruleSetRule : ruleSetRules)
                {
                    Object result = null;

                    if(ruleSetRule.getStatus()==Status.AVAILABLE)
                    {
	                    RuleBean rule = ruleSetRule.getRuleBean();
	             //       StudyBean currentStudy = rule.getStudy();//TODO:Fix me!
	                    StudyBean currentStudy = (StudyBean) getStudyDao().findByPK(rule.getStudyId());
	                    ExpressionBeanObjectWrapper eow = new ExpressionBeanObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet,studySubjectBeanId, studyEventDaoHib, studyEventDefDaoHib);
	                    try {
	                       // StopWatch sw = new StopWatch();
	                        ExpressionObjectWrapper ew = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet);
	                        ew.setStudyEventDaoHib(studyEventDaoHib);
	                        ew.setStudySubjectId(studySubjectBeanId);
	                        ew.setExpressionContext(ExpressionObjectWrapper.CONTEXT_EXPRESSION);
	                        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(ew);
	                       // eow.setUserAccountBean(ub);
	                        eow.setStudyBean(currentStudy);
	                        result = oep.parseAndEvaluateExpression(rule.getExpression().getValue());
	                       // sw.stop();
                            logger.debug( "Rule Expression Evaluation Result: " + result);
	                        // Actions
	                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result.toString());

	                        for (RuleActionBean ruleActionBean: actionListBasedOnRuleExecutionResult){
	                            // ActionProcessor ap =ActionProcessorFacade.getActionProcessor(ruleActionBean.getActionType(), ds, null, null,ruleSet, null, ruleActionBean.getRuleSetRule());
		if (ruleActionBean instanceof EventActionBean){
			beanPropertyService.runAction(ruleActionBean,eow,userId,changeDetails.getRunningInTransaction());
		}
	                        }
	                    }catch (OpenClinicaSystemException osa) {
	                   // 	osa.printStackTrace();
                            logger.error("Rule Runner received exception: " + osa.getMessage());
                            logger.error(ExceptionUtils.getStackTrace(osa));
	                        // TODO: report something useful
	                    }
	                }
	            }
   //     	}
            }
        }
    }

	public boolean checkTargetMatchOld(Integer eventOrdinal, RuleSetBean ruleSet,StudyEventChangeDetails changeDetails)
	{
		Boolean result = true;
	String ruleOrdinal = null;
	String targetOID = ruleSet.getTarget().getValue().substring(0,ruleSet.getTarget().getValue().indexOf("."));
	String targetProperty = ruleSet.getTarget().getValue().substring(ruleSet.getTarget().getValue().indexOf("."));

	//Compare Target rule property (STATUS or STARTDATE) to what has been changed in event.
	//Don't run rule if there isn't a match.
	if (targetProperty.equals(ExpressionService.STARTDATE) && !changeDetails.getStartDateChanged()) result = false;
	else if (targetProperty.equals(ExpressionService.STATUS) && !changeDetails.getStatusChanged()) result = false;

	//For repeating study events, run rule if ordinals match or "ALL" is specified.
	//No brackets implies "ALL" or that the it is not a repeating event, in which case rule should run.
	if (targetOID.contains("["))
	{
		int leftBracketIndex = targetOID.indexOf("[");
		int rightBracketIndex = targetOID.indexOf("]");
		ruleOrdinal =  targetOID.substring(leftBracketIndex + 1,rightBracketIndex);

		if (!(ruleOrdinal.equals("ALL") || Integer.valueOf(ruleOrdinal) == eventOrdinal)) result = false;
	}
		return result;
	}

	public boolean checkTargetMatch(RuleSetBean ruleSet,StudyEventChangeDetails changeDetails)
	{
		Boolean result = true;
	String targetProperty = ruleSet.getTarget().getValue().substring(ruleSet.getTarget().getValue().indexOf("."));

	//Compare Target rule property (STATUS or STARTDATE) to what has been changed in event.
	//Don't run rule if there isn't a match.

	if (targetProperty.equals(ExpressionService.STARTDATE+".A.B") && !changeDetails.getStartDateChanged()){
		result = false;
	}else if (targetProperty.equals(ExpressionService.STATUS+".A.B") && !changeDetails.getStatusChanged()){
		result = false;
	}
		return result;
	}

}
