package org.ohdsi.webapi.cdmresults.keys;

import javax.cache.annotation.GeneratedCacheKey;
import java.util.Objects;

public class DrilldownKey implements GeneratedCacheKey {

    private String domain;
    private String source;
    private Integer conceptId;

    public DrilldownKey(String domain, String source, Integer conceptId) {
        this.domain = domain;
        this.source = source;
        this.conceptId = conceptId;
    }

    public String getDomain() {
        return domain;
    }

    public String getSource() {
        return source;
    }

    public Integer getConceptId() {
        return conceptId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrilldownKey that = (DrilldownKey) o;
        return Objects.equals(domain, that.domain) &&
                Objects.equals(source, that.source) &&
                Objects.equals(conceptId, that.conceptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, source, conceptId);
    }
}
