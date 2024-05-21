package org.ohdsi.webapi.vocabulary;

import org.ohdsi.vocabulary.SearchProvider;
import org.ohdsi.vocabulary.SearchProviderConfig;

public interface VocabularySearchService {
    SearchProvider getSearchProvider(SearchProviderConfig config);
}
