package org.ohdsi.webapi.vocabulary;

import java.util.Arrays;
import java.util.Comparator;

import org.ohdsi.vocabulary.VocabularySearchProvider;
import org.ohdsi.vocabulary.VocabularySearchProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VocabularySearchServiceImpl implements VocabularySearchService {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String NO_PROVIDER_ERROR = "There is no vocabulary search provider which for sourceKey: %s";

    private final VocabularySearchProvider[] vocabularySearchProviders;

    public VocabularySearchServiceImpl(VocabularySearchProvider[] vocabularySearchProviders) {
        this.vocabularySearchProviders = vocabularySearchProviders;
    }

    @Override
    public VocabularySearchProvider getVocabularySearchProvider(VocabularySearchProviderConfig config) {
        return Arrays.stream(vocabularySearchProviders)
                .sorted(Comparator.comparingInt(VocabularySearchProvider::getPriority))
                .filter(p -> p.supports(config.getVersionKey()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format(NO_PROVIDER_ERROR, config.getSourceKey())));
    }
}
