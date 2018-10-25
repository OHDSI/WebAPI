package org.ohdsi.webapi.facets;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

public class FilteredPageRequest extends PageRequest {
    private final List<Filter> filters;

    public FilteredPageRequest(Integer page, Integer size, Sort sort, List<Filter> filters) {
        super(page, size, sort);
        this.filters = filters;
    }

    public List<Filter> getFilters() {
        return filters;
    }
}
