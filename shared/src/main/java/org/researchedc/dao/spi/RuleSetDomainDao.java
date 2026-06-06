package org.researchedc.dao.spi;

import java.util.ArrayList;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.hibernate.ViewRuleAssignmentFilter;
import org.researchedc.dao.hibernate.ViewRuleAssignmentSort;
import org.researchedc.domain.rule.RuleSetBean;

public interface RuleSetDomainDao {
    RuleSetBean findById(Integer id);
    RuleSetBean findById(Integer id, StudyBean study);
    RuleSetBean saveOrUpdate(RuleSetBean ruleSetBean);
    Long count(StudyBean study);
    int getCountWithFilter(ViewRuleAssignmentFilter filter);
    ArrayList<RuleSetBean> getWithFilterAndSort(ViewRuleAssignmentFilter filter, ViewRuleAssignmentSort sort, int rowStart, int rowEnd);
    ArrayList<RuleSetBean> findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(CRFVersionBean crfVersion, CRFBean crfBean,
            StudyBean currentStudy, StudyEventDefinitionBean sed);
    ArrayList<RuleSetBean> findAllByStudy(StudyBean currentStudy);
    ArrayList<RuleSetBean> findByCrf(CRFBean crfBean, StudyBean currentStudy);
    RuleSetBean findByExpression(RuleSetBean ruleSet);
    RuleSetBean findByExpressionAndStudy(RuleSetBean ruleSet, Integer studyId);
    Long getCountByStudy(StudyBean currentStudy);
    ArrayList<RuleSetBean> findAllByStudyEventDef(StudyEventDefinitionBean sed);
    ArrayList<RuleSetBean> findAllEventActions(StudyBean currentStudy);
    ArrayList<RuleSetBean> findAllRunOnSchedules(Boolean schedule);
    ArrayList<RuleSetBean> findAllByStudyEventDefIdWhereItemIsNull(Integer studyEventDefId);
}
