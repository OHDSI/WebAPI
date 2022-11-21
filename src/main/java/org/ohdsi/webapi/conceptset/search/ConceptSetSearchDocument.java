package org.ohdsi.webapi.conceptset.search;

public class ConceptSetSearchDocument {
    private int conceptSetId;
    private long conceptId;
    private String conceptName;
    private String conceptCode;
    private String domainName;

    public int getConceptSetId() {
        return conceptSetId;
    }

    public void setConceptSetId(final int conceptSetId) {
        this.conceptSetId = conceptSetId;
    }

    public long getConceptId() {
        return conceptId;
    }

    public void setConceptId(final long conceptId) {
        this.conceptId = conceptId;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(final String conceptName) {
        this.conceptName = conceptName;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(final String conceptCode) {
        this.conceptCode = conceptCode;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }
}
