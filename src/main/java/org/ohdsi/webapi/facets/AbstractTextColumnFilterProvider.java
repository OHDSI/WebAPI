package org.ohdsi.webapi.facets;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public abstract class AbstractTextColumnFilterProvider implements ColumnFilterProvider {
    @Override
    public <T> Predicate createTextSearchPredicate(String field, String text, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.like(criteriaBuilder.lower(root.get(getField())), '%' + text.toLowerCase() + '%');
    }

    public abstract String getField();
}
