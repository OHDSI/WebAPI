package org.ohdsi.webapi.service.dto;

public class ConceptSetSearchDTO {
    private String query;
    private String[] domainIds;

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public String[] getDomainIds() {
        return domainIds;
    }

    public void setDomainIds(final String[] domainIds) {
        this.domainIds = domainIds;
    }
}
