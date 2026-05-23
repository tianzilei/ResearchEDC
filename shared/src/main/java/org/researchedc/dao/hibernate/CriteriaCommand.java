package org.researchedc.dao.hibernate;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Collections;
import java.util.List;

public interface CriteriaCommand<T> {

    default Predicate execute(CriteriaBuilder cb, Root<T> root) {
        return null;
    }

    default List<Order> executeSort(CriteriaBuilder cb, Root<T> root) {
        return Collections.emptyList();
    }

}
