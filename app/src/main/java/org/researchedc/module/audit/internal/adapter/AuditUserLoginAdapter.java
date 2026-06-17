package org.researchedc.module.audit.internal.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.researchedc.domain.technicaladmin.AuditUserLoginBean;
import org.researchedc.domain.technicaladmin.LoginStatus;
import org.researchedc.module.audit.dto.AuditUserLoginDTO;
import org.researchedc.module.audit.dto.AuditUserLoginQuery;
import org.researchedc.module.audit.service.AuditUserLoginPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
class AuditUserLoginAdapter implements AuditUserLoginPort {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "loginAttemptDate");

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<AuditUserLoginDTO> findUserLogins(AuditUserLoginQuery query) {
        Pageable pageable = query.pageable();
        int rowStart = (int) pageable.getOffset();
        int total = count(query);
        List<AuditUserLoginDTO> rows = findRows(query, rowStart, pageable.getPageSize()).stream()
                .map(this::toDto)
                .toList();
        return new PageImpl<>(rows, pageable, total);
    }

    private int count(AuditUserLoginQuery query) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AuditUserLoginBean> root = cq.from(AuditUserLoginBean.class);
        Predicate predicate = buildPredicate(cb, root, query);
        if (predicate != null) {
            cq.where(predicate);
        }
        cq.select(cb.count(root));
        return entityManager.createQuery(cq).getSingleResult().intValue();
    }

    private List<AuditUserLoginBean> findRows(AuditUserLoginQuery query, int rowStart, int pageSize) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditUserLoginBean> cq = cb.createQuery(AuditUserLoginBean.class);
        Root<AuditUserLoginBean> root = cq.from(AuditUserLoginBean.class);
        Predicate predicate = buildPredicate(cb, root, query);
        if (predicate != null) {
            cq.where(predicate);
        }
        List<Order> orders = toOrders(cb, root, query.pageable());
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }
        TypedQuery<AuditUserLoginBean> typedQuery = entityManager.createQuery(cq);
        typedQuery.setFirstResult(rowStart);
        typedQuery.setMaxResults(pageSize);
        return typedQuery.getResultList();
    }

    private Predicate buildPredicate(CriteriaBuilder cb, Root<AuditUserLoginBean> root, AuditUserLoginQuery query) {
        List<Predicate> predicates = new ArrayList<>();
        addTextPredicate(cb, root, predicates, "userName", query.userName());
        addDatePredicate(cb, root, predicates, query.loginAttemptDate());
        addLoginStatusPredicate(cb, root, predicates, query.loginStatus());
        addTextPredicate(cb, root, predicates, "details", query.details());
        return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
    }

    private void addTextPredicate(CriteriaBuilder cb, Root<AuditUserLoginBean> root, List<Predicate> predicates,
                                  String property, String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get(property).as(String.class)),
                    "%" + value.toLowerCase() + "%"));
        }
    }

    private void addLoginStatusPredicate(CriteriaBuilder cb, Root<AuditUserLoginBean> root,
                                         List<Predicate> predicates, String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(cb.equal(root.get("loginStatus"), LoginStatus.getByName(value)));
        }
    }

    private void addDatePredicate(CriteriaBuilder cb, Root<AuditUserLoginBean> root,
                                  List<Predicate> predicates, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        List<Predicate> ranges = new ArrayList<>();
        addDateRangePredicate(cb, root, value, "yyyy", 1, ranges);
        addDateRangePredicate(cb, root, value, "yyyy-MM", 2, ranges);
        addDateRangePredicate(cb, root, value, "yyyy-MM-dd", 3, ranges);
        addDateRangePredicate(cb, root, value, "yyyy-MM-dd HH", 4, ranges);
        addDateRangePredicate(cb, root, value, "yyyy-MM-dd HH:mm", 5, ranges);
        if (!ranges.isEmpty()) {
            predicates.add(cb.or(ranges.toArray(new Predicate[0])));
        }
    }

    private void addDateRangePredicate(CriteriaBuilder cb, Root<AuditUserLoginBean> root, String value,
                                       String pattern, int plusAmount, List<Predicate> predicates) {
        try {
            DateFormat format = new SimpleDateFormat(pattern);
            Date startDate = format.parse(value);
            DateTime start = new DateTime(startDate.getTime());
            Date endDate = switch (plusAmount) {
                case 1 -> start.plusYears(1).toDate();
                case 2 -> start.plusMonths(1).toDate();
                case 3 -> start.plusDays(1).toDate();
                case 4 -> start.plusHours(1).toDate();
                case 5 -> start.plusMinutes(1).toDate();
                default -> start.toDate();
            };
            predicates.add(cb.between(root.get("loginAttemptDate"), startDate, endDate));
        } catch (Exception ignored) {
        }
    }

    private List<Order> toOrders(CriteriaBuilder cb, Root<AuditUserLoginBean> root, Pageable pageable) {
        Sort requestedSort = pageable.getSort().isSorted() ? pageable.getSort() : DEFAULT_SORT;
        List<Order> orders = new ArrayList<>();
        requestedSort.forEach(order -> orders.add(order.isAscending()
                ? cb.asc(root.get(order.getProperty()))
                : cb.desc(root.get(order.getProperty()))));
        return orders;
    }

    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private AuditUserLoginDTO toDto(AuditUserLoginBean bean) {
        return new AuditUserLoginDTO(
                bean.getId(),
                bean.getUserName(),
                bean.getUserAccountId(),
                bean.getLoginAttemptDate() != null ? bean.getLoginAttemptDate().toInstant().toString() : null,
                bean.getLoginStatus() != null ? bean.getLoginStatus().name() : null,
                bean.getLoginStatus() != null ? bean.getLoginStatus().getCode().toString() : null,
                bean.getDetails());
    }
}
