package org.researchedc.dao.hibernate;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.researchedc.domain.technicaladmin.AuditUserLoginBean;
import org.researchedc.domain.technicaladmin.LoginStatus;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditUserLoginFilter implements CriteriaCommand<AuditUserLoginBean> {

    List<Filter> filters = new ArrayList<Filter>();

    public void addFilter(String property, Object value) {
        filters.add(new Filter(property, value));
    }

    @Override
    public Predicate execute(CriteriaBuilder cb, Root<AuditUserLoginBean> root) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (Filter filter : filters) {
            Predicate p = buildPredicate(cb, root, filter.getProperty(), filter.getValue());
            if (p != null) {
                predicates.add(p);
            }
        }
        if (predicates.isEmpty()) {
            return null;
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate buildPredicate(CriteriaBuilder cb, Root<AuditUserLoginBean> root, String property, Object value) {
        if (value == null) {
            return null;
        }
        if (property.equals("loginStatus")) {
            return cb.equal(root.get(property), LoginStatus.getByName((String) value));
        } else if (property.equals("loginAttemptDate")) {
            return buildDatePredicates(cb, root, String.valueOf(value));
        } else {
            return cb.like(cb.lower(root.get(property).as(String.class)),
                    "%" + String.valueOf(value).toLowerCase() + "%");
        }
    }

    private Predicate buildDatePredicates(CriteriaBuilder cb, Root<AuditUserLoginBean> root, String value) {
        List<Predicate> datePredicates = new ArrayList<Predicate>();
        addDateRangePredicate(cb, root, value, "yyyy", 1, datePredicates);
        addDateRangePredicate(cb, root, value, "yyyy-MM", 2, datePredicates);
        addDateRangePredicate(cb, root, value, "yyyy-MM-dd", 3, datePredicates);
        addDateRangePredicate(cb, root, value, "yyyy-MM-dd HH", 4, datePredicates);
        addDateRangePredicate(cb, root, value, "yyyy-MM-dd HH:mm", 5, datePredicates);
        if (datePredicates.isEmpty()) {
            return null;
        }
        return cb.or(datePredicates.toArray(new Predicate[0]));
    }

    private void addDateRangePredicate(CriteriaBuilder cb, Root<AuditUserLoginBean> root, String value,
            String pattern, int plusAmount, List<Predicate> predicates) {
        try {
            DateFormat format = new SimpleDateFormat(pattern);
            Date startDate = format.parse(value);
            DateTime dt = new DateTime(startDate.getTime());
            DateTime endDt;
            switch (plusAmount) {
                case 1: endDt = dt.plusYears(1); break;
                case 2: endDt = dt.plusMonths(1); break;
                case 3: endDt = dt.plusDays(1); break;
                case 4: endDt = dt.plusHours(1); break;
                case 5: endDt = dt.plusMinutes(1); break;
                default: endDt = dt;
            }
            Date endDate = endDt.toDate();
            predicates.add(cb.between(root.get("loginAttemptDate"), startDate, endDate));
        } catch (Exception e) {
            // Do nothing
        }
    }

    private static class Filter {
        private final String property;
        private final Object value;

        public Filter(String property, Object value) {
            this.property = property;
            this.value = value;
        }

        public String getProperty() {
            return property;
        }

        public Object getValue() {
            return value;
        }
    }

}
