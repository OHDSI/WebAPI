package org.ohdsi.webapi.cdmresults.keys;

import javax.cache.annotation.GeneratedCacheKey;
import java.util.Objects;

public class TreemapKey implements GeneratedCacheKey {

    private String domain;
    private String sourceKey;

    public TreemapKey(String domain, String sourceKey) {
        this.domain = domain;
        this.sourceKey = sourceKey;
    }

    public String getDomain() {
        return domain;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreemapKey that = (TreemapKey) o;
        return Objects.equals(domain, that.domain) &&
                Objects.equals(sourceKey, that.sourceKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, sourceKey);
    }
}
