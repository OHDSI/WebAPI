package org.ohdsi.webapi.vocabulary;

import java.util.Collection;

public interface SearchProvider {
    public abstract boolean supports(VocabularySearchProviderType type);
    public abstract Collection<Concept> executeSearch(SearchProviderConfig config, String query, String rows) throws Exception;
}
