package org.ohdsi.webapi.facets;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class FilteredPageRequest extends PageRequest {
    private final Filter filter;

    public FilteredPageRequest(Integer page, Integer size, Sort sort, Filter filter) {
        super(page, size, sort);
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }
}
