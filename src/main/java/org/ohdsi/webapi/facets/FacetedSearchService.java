package org.ohdsi.webapi.facets;

import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

;

@Service
public class FacetedSearchService {
    private final Collection<FacetProvider> providers;
    private final Map<String, FacetProvider> providersByFacet = new HashMap<>();

    @PostConstruct
    private void init() {
        providers.forEach(p -> providersByFacet.put(p.getName(), p));
    }


    public FacetedSearchService(Collection<FacetProvider> providers) {
        this.providers = providers;
    }

    public List<FilterItem> getValues(String facet, String entityName) {
        final FacetProvider facetProvider = providersByFacet.get(facet);
        if(facetProvider == null) {
            throw new IllegalArgumentException("unknown facet");
        }
        return facetProvider.getValues(entityName);

    }

    public <T> Page<T> getPage(FilteredPageRequest pageable, JpaSpecificationExecutor<T> repository) {
        try {
            return repository.findAll(createFilter(pageable), pageable);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("getting page", e);
            throw e;
        }
    }

    private <T> Specification<T> createFilter(FilteredPageRequest request) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if(request.getSort() != null) {
                criteriaQuery.orderBy(QueryUtils.toOrders(request.getSort(), root, criteriaBuilder));
            }
            final List<Predicate> predicates = new ArrayList<>();
            request.getFilters().forEach(filter -> {
                if(!filter.selectedItems.isEmpty()) {
                    final FacetProvider provider = providersByFacet.get(filter.name);
                    if (provider == null) {
                        throw new IllegalArgumentException("unknown facet");
                    }
                    predicates.add(provider.createPredicate(filter.selectedItems, criteriaBuilder, root));
                }
            });
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }


}
