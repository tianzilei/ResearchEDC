package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.rule.action.PropertyBean;

public class RuleActionPropertyDao extends AbstractDomainDao<PropertyBean> {

    @Override
    public Class<PropertyBean> domainClass() {
        return PropertyBean.class;
    }

    public ArrayList <PropertyBean> findByOid(String itemOid , String groupOid) {
        String query = "from " + getDomainClassName() +  "  where oc_oid = :itemOid OR oc_oid=:groupOid ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("itemOid", itemOid);
        q.setParameter("groupOid", groupOid);
        return (ArrayList <PropertyBean>) q.list();
    }

    public ArrayList <PropertyBean> findByOid(String Oid) {
        String query = "from " + getDomainClassName() +  "  where oc_oid=:Oid ";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("Oid", Oid);
        return (ArrayList <PropertyBean>) q.list();
    }


}
