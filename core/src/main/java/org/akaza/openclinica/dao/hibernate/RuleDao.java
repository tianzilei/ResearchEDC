package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleBean;

public class RuleDao extends AbstractDomainDao<RuleBean> {

    @Override
    public Class<RuleBean> domainClass() {
        return RuleBean.class;
    }

    public RuleBean findByOid(RuleBean ruleBean) {
        String query = "from " + getDomainClassName() + " rule  where rule.oid = :oid and  rule.studyId = :studyId ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("oid", ruleBean.getOid());
        q.setParameter("studyId", ruleBean.getStudyId());
        return (RuleBean) q.uniqueResult();
    }

    public RuleBean findByOid(String oid, Integer studyId) {
        String query = "from " + getDomainClassName() + " rule  where rule.oid = :oid and  rule.studyId = :studyId ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("oid", oid);
        q.setParameter("studyId", studyId);
        return (RuleBean) q.uniqueResult();
    }

}
