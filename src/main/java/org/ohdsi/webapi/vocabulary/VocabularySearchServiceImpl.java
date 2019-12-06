package org.ohdsi.webapi.vocabulary;

import java.util.HashSet;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.util.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class VocabularySearchServiceImpl implements VocabularySearchService {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private static HashSet<String> availableVocabularyFullTextIndices = new HashSet<>();
    private final List<SearchProvider> searchProviderList;
    
    private static final String NO_PROVIDER_ERROR = "There is no vocabulary search provider which for sourceKey: %s";
    
    @Value("${solr.endpoint}")
    private String solrEndpoint;
    
    @Autowired
    VocabularyService vocabService;
    
    @PostConstruct
    protected void init() {
        // Get the SOLR cores list if enabled
        if (!StringUtils.isEmpty(solrEndpoint)) {
          try {
            availableVocabularyFullTextIndices = SolrUtils.getCores(solrEndpoint);
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
