package org.ohdsi.webapi.vocabulary;

import java.util.Collection;

import org.ohdsi.vocabulary.Concept;
import org.ohdsi.vocabulary.SearchProvider;
import org.ohdsi.vocabulary.SearchProviderConfig;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSearchProvider implements SearchProvider {
    @Autowired
    private SourceRepository sourceRepository;

    private final static int VOCABULARY_PRIORITY = Integer.MAX_VALUE;

    @Autowired
    VocabularyService vocabService;
    
    @Override
    public boolean supports(String vocabularyVersionKey) {
        return true;
    }

    @Override
    public int getPriority() {
        return VOCABULARY_PRIORITY;
    }

    @Override
    public Collection<Concept> executeSearch(SearchProviderConfig config, String query, String rows) throws Exception {
        Source source = sourceRepository.findBySourceKey(config.getSourceKey());

        PreparedStatementRenderer psr = vocabService.prepareExecuteSearchWithQuery(query, source);
        return vocabService.getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), vocabService.getRowMapper());
    }
}
