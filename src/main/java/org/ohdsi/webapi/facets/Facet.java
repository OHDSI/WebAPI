package org.ohdsi.webapi.facets;

import java.util.List;

public class Facet {
    public String name;
    public List<FacetItem> selectedItems;

    public Facet(String name, List<FacetItem> items) {
        this.name = name;
        this.selectedItems = items;
    }

    public Facet() {
    }
}
