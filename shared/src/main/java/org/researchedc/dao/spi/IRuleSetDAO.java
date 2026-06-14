package org.researchedc.dao.spi;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.rule.RuleSetBean;
import org.researchedc.bean.submit.CRFVersionBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface IRuleSetDAO {
    EntityBean findByPK(int ID);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    ArrayList<RuleSetBean> findAllByStudy(StudyBean currentStudy);
    ArrayList<RuleSetBean> findByCrf(CRFBean crfBean, StudyBean currentStudy);
    ArrayList<RuleSetBean> findByCrfVersionStudyAndStudyEventDefinition(CRFVersionBean crfVersionBean, StudyBean currentStudy, StudyEventDefinitionBean sed);
    ArrayList<RuleSetBean> findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(CRFVersionBean crfVersion, CRFBean crfBean, StudyBean currentStudy, StudyEventDefinitionBean sed);
    ArrayList<RuleSetBean> findByCrfStudyAndStudyEventDefinition(CRFBean crfBean, StudyBean currentStudy, StudyEventDefinitionBean sed);
    RuleSetBean findByExpression(RuleSetBean ruleSetBean);
    RuleSetBean findByStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    EntityBean remove(RuleSetBean ruleSetBean, UserAccountBean ub);
    EntityBean restore(RuleSetBean ruleSetBean, UserAccountBean ub);
    Object getEntityFromHashMap(HashMap hm);
}
