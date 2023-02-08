package org.ohdsi.webapi.vocabulary;

import org.ohdsi.webapi.vocabulary.Concept;
import org.ohdsi.webapi.vocabulary.SearchProviderConfig;

import java.util.Collection;

public interface SearchProvider<T extends SearchProviderConfig> {
    boolean supports(String vocabularyVersionKey);
    int getPriority();
    Collection<Concept> executeSearch(T config, String query, String rows) throws Exception;
}
