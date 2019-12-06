package org.ohdsi.webapi.vocabulary;

import java.util.HashSet;
import java.util.List;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.service.VocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VocabularySearchServiceImpl implements VocabularySearchService {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private static HashSet<String> availableVocabularyFullTextIndices = new HashSet<>();
    private final List<SearchProvider> searchProviderList;
    
    private static final String NO_PROVIDER_ERROR = "There is no vocabulary search provider which for sourceKey: %s";
    
    @Autowired
    VocabularyService vocabService;
    
    @Autowired
    SolrSearchClient solrSearchClient;
    
    @PostConstruct
    protected void init() {
        // Get the SOLR cores list if enabled
        if (solrSearchClient.enabled()) {
          try {
            availableVocabularyFullTextIndices = solrSearchClient.getCores();
          } catch (Exception ex) {
            log.error("SOLR Core Initialization Error:  WebAPI was unable to obtain the list of available cores.", ex);
          }
        }
    }
    
    public VocabularySearchServiceImpl(List<SearchProvider> searchProviderList) {
        this.searchProviderList = searchProviderList;
    }
    
    @Override
    public SearchProvider getSearchProvider(SearchProviderConfig config) {
        VocabularySearchProviderType type = VocabularySearchProviderType.DATABASE;
        if (availableVocabularyFullTextIndices.contains(config.getVersionKey())) {
            type = VocabularySearchProviderType.SOLR;
        }
        return selectSearchProvider(type, config);
    }
    
    private SearchProvider selectSearchProvider(VocabularySearchProviderType type, SearchProviderConfig config) {
        return searchProviderList.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format(NO_PROVIDER_ERROR, config.getSource().getSourceKey())));        
    }
}
