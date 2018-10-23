package com.jnj.honeur.webapi.source;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.source.SourceDaimon;

import java.util.Objects;

/**
 * Represents the context needed to identify a source daimon
 *
 * @author Peter Moorthamer
 * Date: 02/feb/2018
 */
public class SourceDaimonContext {

    private static final Log LOG = LogFactory.getLog(SourceDaimonContext.class);

    public static final String SOURCE_DAIMON_CONTEXT_KEY_SEPARATOR = ":";

    private String sourceKey;
    private SourceDaimon.DaimonType daimonType;

    public SourceDaimonContext(String sourceKey, SourceDaimon.DaimonType daimonType) {
        this.sourceKey = sourceKey;
        this.daimonType = daimonType;
    }

    public SourceDaimonContext(String sourceDaimonContextKey) {
        LOG.debug(String.format("New SourceDaimonContext %s", sourceDaimonContextKey));
        if(StringUtils.isBlank(sourceDaimonContextKey)) {
            throw new IllegalArgumentException("Invalid source daimon context key");
        }
        String[] sourceDaimonContextParts = getSourceDaimonContextKeyParts(sourceDaimonContextKey);
        if(sourceDaimonContextParts.length != 2) {
            throw new IllegalArgumentException("Invalid source daimon context key");
        }
        this.sourceKey = sourceDaimonContextParts[0];
        try {
            this.daimonType = SourceDaimon.DaimonType.valueOf(sourceDaimonContextParts[1]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid source daimon context key");
        }
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public SourceDaimon.DaimonType getDaimonType() {
        return daimonType;
    }

    private String[] getSourceDaimonContextKeyParts(final String sourceDaimonContextKey) {
        return sourceDaimonContextKey.split(SOURCE_DAIMON_CONTEXT_KEY_SEPARATOR);
    }

    public String getSourceDaimonContextKey() {
        return sourceKey + SOURCE_DAIMON_CONTEXT_KEY_SEPARATOR + daimonType.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceDaimonContext that = (SourceDaimonContext) o;
        return Objects.equals(sourceKey, that.sourceKey) &&
                daimonType == that.daimonType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceKey, daimonType);
    }

    @Override
    public String toString() {
        return getSourceDaimonContextKey();
    }
}
