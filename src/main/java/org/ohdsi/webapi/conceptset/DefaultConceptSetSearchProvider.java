package org.ohdsi.webapi.conceptset;

import java.util.Collection;
import java.util.Set;

import org.ohdsi.conceptset.ConceptSetSearchDocument;
import org.ohdsi.conceptset.ConceptSetSearchProvider;
import org.springframework.stereotype.Service;

@Service
public class DefaultConceptSetSearchProvider implements ConceptSetSearchProvider {
	
    private final static int DEFAULT_SEARCH_PRIORITY = Integer.MAX_VALUE;

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

    @Override
    public int getPriority() {
        return DEFAULT_SEARCH_PRIORITY;
    }
}
