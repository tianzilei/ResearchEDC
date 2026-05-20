package org.researchedc.dao.hibernate;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

import org.researchedc.domain.technicaladmin.AuditUserLoginBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuditUserLoginSort implements CriteriaCommand<AuditUserLoginBean> {
    List<Sort> sorts = new ArrayList<Sort>();

    public void addSort(String property, String order) {
        sorts.add(new Sort(property, order));
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    @Override
    public List<Order> executeSort(CriteriaBuilder cb, Root<AuditUserLoginBean> root) {
        if (sorts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Order> orders = new ArrayList<Order>();
        for (Sort sort : sorts) {
            if (sort.getOrder().equals(Sort.ASC)) {
                orders.add(cb.asc(root.get(sort.getProperty())));
            } else if (sort.getOrder().equals(Sort.DESC)) {
                orders.add(cb.desc(root.get(sort.getProperty())));
            }
        }
        return orders;
    }

    private static class Sort {
        public final static String ASC = "asc";
        public final static String DESC = "desc";

        private final String property;
        private final String order;

        public Sort(String property, String order) {
            this.property = property;
            this.order = order;
        }

        public String getProperty() {
            return property;
        }

        public String getOrder() {
            return order;
        }
    }
}
