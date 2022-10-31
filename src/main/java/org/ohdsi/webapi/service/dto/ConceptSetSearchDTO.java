package org.ohdsi.webapi.service.dto;

public class ConceptSetSearchDTO {
    private String query;
    private String[] domainId;

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public String[] getDomainId() {
        return domainId;
    }

    public void setDomainId(final String[] domainId) {
        this.domainId = domainId;
    }
}
