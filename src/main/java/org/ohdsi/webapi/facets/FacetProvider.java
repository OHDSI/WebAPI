package org.ohdsi.webapi.facets;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public interface FacetProvider {
    String getName();

    List<FacetItem> getValues(String entityName);

    <T> Predicate createFacetPredicate(List<FacetItem> items, CriteriaBuilder criteriaBuilder, Root<T> root);

    <T> Predicate createTextSearchPredicate(String field, String text, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
}
