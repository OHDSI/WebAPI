package org.ohdsi.webapi.facets;

import java.util.List;
import java.util.Map;

public class Filter {
    private String text;
    private Map<String, List<String>> facets;

    public Filter(String text, Map<String, List<String>> facets) {
        this.text = text;
        this.facets = facets;
    }

    public Filter() {
    }

    public String getText() {
        return text;
    }

    public Map<String, List<String>> getFacets() {
        return facets;
    }
}
