package org.ohdsi.webapi.facets;

import java.util.List;

public class Filter {
    private String text;
    private List<String> searchableFields;
    private List<Facet> facets;

    public Filter(String text, List<String> searchableFields, List<Facet> facets) {
        this.text = text;
        this.searchableFields = searchableFields;
        this.facets = facets;
    }

    public Filter() {
    }

    public String getText() {
        return text;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public List<String> getSearchableFields() {
        return searchableFields;
    }
}
