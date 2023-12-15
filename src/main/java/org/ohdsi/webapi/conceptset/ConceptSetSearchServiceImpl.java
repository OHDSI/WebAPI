package org.ohdsi.webapi.conceptset;

import java.util.Arrays;
import java.util.Comparator;

import org.ohdsi.conceptset.ConceptSetSearchProvider;
import org.ohdsi.vocabulary.VocabularySearchProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConceptSetSearchServiceImpl implements ConceptSetSearchService {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String NO_PROVIDER_ERROR = "There is no concept set search provider";
    
    private final ConceptSetSearchProvider[] conceptSetSearchProviders;

    public ConceptSetSearchServiceImpl(ConceptSetSearchProvider[] conceptSetSearchProviders) {
        this.conceptSetSearchProviders = conceptSetSearchProviders;
    }

    @Override
    public ConceptSetSearchProvider getConceptSetSearchProvider() {
    	return Arrays.stream(conceptSetSearchProviders)
                .sorted(Comparator.comparingInt(ConceptSetSearchProvider::getPriority))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format(NO_PROVIDER_ERROR)));
    }

}
