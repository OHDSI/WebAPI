package org.ohdsi.webapi.vocabulary;

import java.util.Collection;

import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.extcommon.vocabulary.SearchProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSearchProvider implements SearchProvider {
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
        PreparedStatementRenderer psr = vocabService.prepareExecuteSearchWithQuery(query, config.getSource());
        return vocabService.getSourceJdbcTemplate(config.getSource()).query(psr.getSql(), psr.getSetter(), vocabService.getRowMapper());
    }
}
