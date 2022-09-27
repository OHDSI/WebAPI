package org.ohdsi.webapi.vocabulary;

import java.util.Collection;
import java.util.Objects;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSearchProvider implements SearchProvider {
    @Autowired
    VocabularyService vocabService;
    
    @Override
    public boolean supports(VocabularySearchProviderType type) {
        return Objects.equals(type, VocabularySearchProviderType.DATABASE);
    }
    
    @Override
    public Collection<Concept> executeSearch(SearchProviderConfig config, String query, String rows) throws Exception {
      PreparedStatementRenderer psr = vocabService.prepareExecuteSearchWithQuery(query, config.getSource());
        return vocabService.getSourceJdbcTemplate(config.getSource()).query(psr.getSql(), psr.getSetter(), vocabService.getRowMapper());
    }
}
