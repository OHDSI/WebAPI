package org.ohdsi.webapi.conceptset;

import java.util.Collection;
import java.util.Set;

import org.ohdsi.conceptset.ConceptSetSearchDocument;
import org.ohdsi.conceptset.ConceptSetSearchProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConceptSetSearchServiceImpl implements ConceptSetSearchService {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ConceptSetSearchProvider conceptSetSearchProvider;

    public ConceptSetSearchServiceImpl(ConceptSetSearchProvider conceptSetSearchProvider) {
        this.conceptSetSearchProvider = conceptSetSearchProvider;
    }

    @Override
    public ConceptSetSearchProvider getConceptSearchProvider() {
        return conceptSetSearchProvider != null ? conceptSetSearchProvider : new DefaultConceptSetSearchProvider();
    }

	private static final class DefaultConceptSetSearchProvider implements ConceptSetSearchProvider {

        @Override
        public Set<Integer> executeSearch(String query, String[] domainIds) {
           throw new UnsupportedOperationException("The Advanced Concept Set Search feature is available only if Apache Solr is configured properly");
        }

        @Override
        public boolean isSearchAvailable() {
            return false;
        }

        @Override
        public void clearConceptSetIndex() {
            throw new UnsupportedOperationException("The Advanced Concept Set Search feature is available only if Apache Solr is configured properly");
        }

        @Override
        public void reindexConceptSet(Integer conceptSetId, Collection<ConceptSetSearchDocument> documents) {
            throw new UnsupportedOperationException("The Advanced Concept Set Search feature is available only if Apache Solr is configured properly");
        }

        @Override
        public void deleteConceptSetIndex(Integer id) {
            throw new UnsupportedOperationException("The Advanced Concept Set Search feature is available only if Apache Solr is configured properly");
        }
    }
}
