package org.ohdsi.webapi.cdmresults.keys;

import javax.cache.annotation.GeneratedCacheKey;
import java.util.Objects;

public class RefreshableSourceKey implements GeneratedCacheKey {

    private String source;
    private Boolean refresh;

    public RefreshableSourceKey(String source, Boolean refresh) {
        this.source = source;
        this.refresh = refresh;
    }

    public String getSource() {
        return source;
    }

    public Boolean getRefresh() {
        return refresh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshableSourceKey that = (RefreshableSourceKey) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(refresh, that.refresh);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, refresh);
    }
}
