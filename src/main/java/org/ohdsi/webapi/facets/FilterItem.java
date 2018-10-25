package org.ohdsi.webapi.facets;

public class FilterItem {
    public String text;
    public String key;
    public Integer count;

    public FilterItem(String text, String key, Integer count) {
        this.text = text;
        this.key = key;
        this.count = count;
    }

    public FilterItem() {}
}
