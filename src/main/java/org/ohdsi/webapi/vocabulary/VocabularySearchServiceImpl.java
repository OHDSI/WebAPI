package org.ohdsi.webapi.vocabulary;

import java.util.Arrays;
import java.util.Comparator;

import org.ohdsi.vocabulary.SearchProvider;
import org.ohdsi.vocabulary.SearchProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VocabularySearchServiceImpl implements VocabularySearchService {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String NO_PROVIDER_ERROR = "There is no vocabulary search provider which for sourceKey: %s";

    private final SearchProvider[] searchProviders;

    public VocabularySearchServiceImpl(SearchProvider[] searchProviders) {
        this.searchProviders = searchProviders;
    }

    @Override
    public SearchProvider getSearchProvider(SearchProviderConfig config) {
        return Arrays.stream(searchProviders)
                .sorted(Comparator.comparingInt(SearchProvider::getPriority))
                .filter(p -> p.supports(config.getVersionKey()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format(NO_PROVIDER_ERROR, config.getSourceKey())));
    }
}
