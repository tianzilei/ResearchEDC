package org.researchedc.module.audit.internal.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.researchedc.dao.hibernate.AuditUserLoginFilter;
import org.researchedc.dao.hibernate.AuditUserLoginSort;
import org.researchedc.dao.spi.AuditUserLoginDao;
import org.researchedc.domain.technicaladmin.AuditUserLoginBean;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Primary adapter implementing {@link AuditUserLoginDao} (legacy SPI)
 * using JPA Criteria API with injected {@link EntityManager}.
 *
 * <p>Replaces the hibernate {@code AuditUserLoginDao} implementation so the
 * HibernateConfig {@code @Bean} and the hibernate impl file can be deleted.</p>
 */
@Component("auditUserLoginDao")
@Primary
@Transactional(readOnly = true)
public class AuditUserLoginDaoAdapter implements AuditUserLoginDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public int getCountWithFilter(final AuditUserLoginFilter filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AuditUserLoginBean> root = cq.from(AuditUserLoginBean.class);

        Predicate predicate = filter.execute(cb, root);
        if (predicate != null) {
            cq.where(predicate);
        }
        cq.select(cb.count(root));

        return entityManager.createQuery(cq).getSingleResult().intValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<AuditUserLoginBean> getWithFilterAndSort(
            final AuditUserLoginFilter filter,
            final AuditUserLoginSort sort,
            final int rowStart,
            final int rowEnd) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditUserLoginBean> cq = cb.createQuery(AuditUserLoginBean.class);
        Root<AuditUserLoginBean> root = cq.from(AuditUserLoginBean.class);

        Predicate predicate = filter.execute(cb, root);
        if (predicate != null) {
            cq.where(predicate);
        }

        List<Order> orders = sort.executeSort(cb, root);
        if (orders != null && !orders.isEmpty()) {
            cq.orderBy(orders);
        }

        TypedQuery<AuditUserLoginBean> query = entityManager.createQuery(cq);
        query.setFirstResult(rowStart);
        query.setMaxResults(rowEnd - rowStart);
        return (ArrayList<AuditUserLoginBean>) query.getResultList();
    }
}
