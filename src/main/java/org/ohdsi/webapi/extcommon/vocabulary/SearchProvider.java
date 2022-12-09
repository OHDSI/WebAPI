package org.ohdsi.webapi.extcommon.vocabulary;

import org.ohdsi.webapi.vocabulary.Concept;
import org.ohdsi.webapi.vocabulary.SearchProviderConfig;

import java.util.Collection;

public interface SearchProvider {
    boolean supports(String vocabularyVersionKey);
    int getPriority();
    Collection<Concept> executeSearch(SearchProviderConfig config, String query, String rows) throws Exception;
}
