package org.ohdsi.webapi.facets;

public class FacetItem {
    public String text;
    public String key;
    public Integer count;

    public FacetItem(String text, String key, Integer count) {
        this.text = text;
        this.key = key;
        this.count = count;
    }

    public FacetItem() {}
}
