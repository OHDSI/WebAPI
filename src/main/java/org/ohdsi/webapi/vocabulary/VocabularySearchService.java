package org.ohdsi.webapi.vocabulary;

import org.ohdsi.vocabulary.VocabularySearchProvider;
import org.ohdsi.vocabulary.VocabularySearchProviderConfig;

public interface VocabularySearchService {
    VocabularySearchProvider getVocabularySearchProvider(VocabularySearchProviderConfig config);
}
