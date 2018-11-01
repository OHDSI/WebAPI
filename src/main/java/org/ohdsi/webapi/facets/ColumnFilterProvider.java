package org.ohdsi.webapi.facets;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public interface ColumnFilterProvider {
    String getName();

    <T> Predicate createTextSearchPredicate(String field, String text, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
}
