package org.researchedc.dao.hibernate;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.spi.RuleSetDomainDao;
import org.researchedc.domain.rule.RuleSetBean;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RuleSetDao extends AbstractDomainDao<RuleSetBean> implements RuleSetDomainDao {

    @Override
    public Class<RuleSetBean> domainClass() {
        return RuleSetBean.class;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public ArrayList<RuleSetBean> findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(CRFVersionBean crfVersion, CRFBean crfBean, StudyBean currentStudy,
            StudyEventDefinitionBean sed) {
        // Using a sql query because we are referencing objects not managed by hibernate
        String query =
            " select rs.* from rule_set rs where rs.study_id = :studyId " + " AND (( rs.study_event_definition_id = :studyEventDefinitionId "
                + " AND (( rs.crf_version_id = :crfVersionId AND rs.crf_id = :crfId ) "
                + " OR (rs.crf_version_id is null AND rs.crf_id = :crfId ))) OR ( rs.study_event_definition_id is null "
                + " and rs.item_id in (select item_id from item_form_metadata where crf_version_id = :crfVersionId)  ))";
        org.hibernate.query.Query q = getCurrentSession().createNativeQuery(query, domainClass());
        q.setParameter("crfVersionId", crfVersion.getId());
        q.setParameter("crfId", crfBean.getId());
        q.setParameter("studyId", currentStudy.getParentStudyId() != 0 ? currentStudy.getParentStudyId() : currentStudy.getId());
        q.setParameter("studyEventDefinitionId", sed.getId());
        q.setCacheable(true);

        return (ArrayList<RuleSetBean>) q.list();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetBean> findAllByStudy(StudyBean currentStudy) {
        String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.studyId = :studyId  ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyId", currentStudy.getId());
        return (ArrayList<RuleSetBean>) q.list();
    }
    
   

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetBean> findByCrf(CRFBean crfBean, StudyBean currentStudy) {
        String query =
            " select rs.* from rule_set rs where rs.study_id = :studyId "
                + " AND rs.item_id in ( select distinct(item_id) from item_form_metadata ifm,crf_version cv "
                + " where ifm.crf_version_id = cv.crf_version_id and cv.crf_id = :crfId) ";
        // Using a sql query because we are referencing objects not managed by hibernate
        org.hibernate.query.Query q = getCurrentSession().createNativeQuery(query, domainClass());
        q.setParameter("crfId", crfBean.getId());
        q.setParameter("studyId", currentStudy.getId());
        return (ArrayList<RuleSetBean>) q.list();
    }


    public RuleSetBean findByExpressionAndStudy(RuleSetBean ruleSet, Integer studyId) {
        String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.originalTarget.value = :value " +
        		"AND ruleSet.originalTarget.context = :context " +
        		"AND ruleSet.studyId = :studyId ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("value", ruleSet.getTarget().getValue());
        q.setParameter("context", ruleSet.getTarget().getContext());
        q.setParameter("studyId", studyId);
        return (RuleSetBean) q.uniqueResult();
    }

    public ArrayList<RuleSetBean> findAllEventActions(StudyBean currentStudy){
    	String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.originalTarget.value LIKE '%.STARTDATE%' or ruleSet.originalTarget.value LIKE '%.STATUS%' and ruleSet.studyId = :studyId ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyId", currentStudy.getId());
        return (ArrayList<RuleSetBean>) q.list();
    }

}
