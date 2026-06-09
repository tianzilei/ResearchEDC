package org.researchedc.dao.spi;

import java.util.ArrayList;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.domain.rule.RuleSetBean;

public interface RuleSetDomainDao {
    RuleSetBean findById(Integer id);
    RuleSetBean saveOrUpdate(RuleSetBean ruleSetBean);
    ArrayList<RuleSetBean> findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(CRFVersionBean crfVersion, CRFBean crfBean,
            StudyBean currentStudy, StudyEventDefinitionBean sed);
    ArrayList<RuleSetBean> findAllByStudy(StudyBean currentStudy);
    ArrayList<RuleSetBean> findByCrf(CRFBean crfBean, StudyBean currentStudy);
    RuleSetBean findByExpressionAndStudy(RuleSetBean ruleSet, Integer studyId);
    ArrayList<RuleSetBean> findAllEventActions(StudyBean currentStudy);
}
