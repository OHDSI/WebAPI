package org.ohdsi.webapi.vocabulary;

public interface VocabularySearchService {
    SearchProvider getSearchProvider(SearchProviderConfig config);
}
