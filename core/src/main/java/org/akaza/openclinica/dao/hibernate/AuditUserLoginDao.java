package org.akaza.openclinica.dao.hibernate;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;

import java.util.ArrayList;
import java.util.List;

public class AuditUserLoginDao extends AbstractDomainDao<AuditUserLoginBean> {

    @Override
    public Class<AuditUserLoginBean> domainClass() {
        return AuditUserLoginBean.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<AuditUserLoginBean> findAll() {
        return (ArrayList<AuditUserLoginBean>) super.findAll();
    }

    public int getCountWithFilter(final AuditUserLoginFilter filter) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AuditUserLoginBean> root = cq.from(domainClass());

        Predicate predicate = filter.execute(cb, root);
        if (predicate != null) {
            cq.where(predicate);
        }
        cq.select(cb.count(root));

        return getEntityManager().createQuery(cq).getSingleResult().intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<AuditUserLoginBean> getWithFilterAndSort(final AuditUserLoginFilter filter, final AuditUserLoginSort sort, final int rowStart,
            final int rowEnd) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<AuditUserLoginBean> cq = cb.createQuery(domainClass());
        Root<AuditUserLoginBean> root = cq.from(domainClass());

        Predicate predicate = filter.execute(cb, root);
        if (predicate != null) {
            cq.where(predicate);
        }

        List<Order> orders = sort.executeSort(cb, root);
        if (orders != null && !orders.isEmpty()) {
            cq.orderBy(orders);
        }

        TypedQuery<AuditUserLoginBean> query = getEntityManager().createQuery(cq);
        query.setFirstResult(rowStart);
        query.setMaxResults(rowEnd - rowStart);
        return (ArrayList<AuditUserLoginBean>) query.getResultList();
    }

}
