package org.ohdsi.webapi.facets;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

;

@Service
public class FacetedSearchService {
    private final Map<String, FacetProvider> providersByFacet = new HashMap<>();

    public FacetedSearchService(Collection<FacetProvider> providers) {
        providers.forEach(p -> {
            providersByFacet.put(p.getName(), p);
        });
    }

    public List<FacetItem> getValues(String facet, String entityName) {
        return getFacetProvider(facet).getValues(entityName);
    }

    public <T> Page<T> getPage(FilteredPageRequest pageable, JpaSpecificationExecutor<T> repository) {
        try {
            return repository.findAll(createFilter(pageable), pageable);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("getting page", e);
            throw e;
        }
    }

    public <T> Page<T> getPage(FilteredPageRequest pageable, EntityGraphJpaSpecificationExecutor<T> repository, EntityGraph defaultEntityGraph) {
        try {
            return repository.findAll(createFilter(pageable), pageable, defaultEntityGraph);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("getting page", e);
            throw e;
        }
    }

    private <T> Specification<T> createFilter(FilteredPageRequest request) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (request.getSort() != null) {
                criteriaQuery.orderBy(QueryUtils.toOrders(request.getSort(), root, criteriaBuilder));
            }
            final Predicate facetSearch = createFacetSearchPredicate(root, criteriaBuilder, request.getFilter());
            return StringUtils.isNotBlank(request.getFilter().getText())
                    ? criteriaBuilder.and(facetSearch, createTextSearchPredicate(root, criteriaBuilder, request.getFilter()))
                    : facetSearch;
        };
    }

    private <T> Predicate createTextSearchPredicate(Root<T> root, CriteriaBuilder criteriaBuilder, Filter filter) {
        return criteriaBuilder.or(filter.getSearchableFields().stream()
                .map(field -> getFacetProvider(field).createTextSearchPredicate(field, filter.getText(), criteriaBuilder, root))
                .toArray(Predicate[]::new));
    }

    private <T> Predicate createFacetSearchPredicate(Root<T> root, CriteriaBuilder criteriaBuilder, Filter filter) {
        return criteriaBuilder.and(filter.getFacets().stream()
                .filter(facet -> !facet.selectedItems.isEmpty())
                .map(facet -> getFacetProvider(facet.name).createFacetPredicate(facet.selectedItems, criteriaBuilder, root))
                .toArray(Predicate[]::new));
    }

    private FacetProvider getFacetProvider(String facet) {
        final FacetProvider facetProvider = providersByFacet.get(facet);
        if (facetProvider == null) {
            throw new IllegalArgumentException("unknown facet: " + facet);
        }
        return facetProvider;
    }


}
