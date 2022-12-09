package org.ohdsi.webapi.vocabulary;

import org.ohdsi.webapi.extcommon.vocabulary.SearchProvider;

public interface VocabularySearchService {
    SearchProvider getSearchProvider(SearchProviderConfig config);
}
