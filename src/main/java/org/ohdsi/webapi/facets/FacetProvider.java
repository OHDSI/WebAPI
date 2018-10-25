package org.ohdsi.webapi.facets;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public interface FacetProvider {
    String getName();

    List<FilterItem> getValues(String entityName);

    Predicate createPredicate(List<FilterItem> items, CriteriaBuilder criteriaBuilder, Root root);
}
