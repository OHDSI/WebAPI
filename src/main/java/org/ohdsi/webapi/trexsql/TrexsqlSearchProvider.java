package org.ohdsi.webapi.trexsql;

import org.ohdsi.vocabulary.Concept;
import org.ohdsi.vocabulary.SearchProvider;
import org.ohdsi.vocabulary.SearchProviderConfig;
import org.ohdsi.webapi.trexsql.exception.CacheNotFoundException;
import org.ohdsi.webapi.trexsql.exception.TrexsqlNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * SearchProvider implementation using TrexSQL.
 */
@Component
@ConditionalOnProperty(name = "trexsql.enabled", havingValue = "true", matchIfMissing = false)
public class TrexsqlSearchProvider implements SearchProvider {

    private static final Logger log = LoggerFactory.getLogger(TrexsqlSearchProvider.class);

    private static final int TREXSQL_PRIORITY = 1;

    private final TrexsqlService trexsqlService;
    private final TrexsqlConfig config;

    public TrexsqlSearchProvider(TrexsqlService trexsqlService, TrexsqlConfig config) {
        this.trexsqlService = trexsqlService;
        this.config = config;
    }

    @Override
    public boolean supports(String vocabularyVersionKey) {
        return config.isEnabled();
    }

    @Override
    public int getPriority() {
        return TREXSQL_PRIORITY;
    }

    @Override
    public Collection<Concept> executeSearch(SearchProviderConfig searchConfig, String query, String rows) throws Exception {
        String sourceKey = searchConfig.getSourceKey();

        if (!trexsqlService.isEnabledForSource(sourceKey)) {
            log.debug("TrexSQL not enabled for source {}", sourceKey);
            throw new TrexsqlNotAvailableException(sourceKey, "TrexSQL not enabled for source: " + sourceKey);
        }

        if (!trexsqlService.isCacheAvailable(sourceKey)) {
            log.debug("Cache not available for source {}", sourceKey);
            throw new CacheNotFoundException(sourceKey);
        }

        int maxRows = parseRows(rows);
        log.debug("TrexSQL search for source {} with query: {}", sourceKey, query);

        try {
            List<Map<String, Object>> results = trexsqlService.searchVocab(sourceKey, query, maxRows);
            return mapToConcepts(results);
        } catch (TrexsqlNotAvailableException | CacheNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("TrexSQL search failed for source {}: {}", sourceKey, e.getMessage(), e);
            throw new RuntimeException("TrexSQL search failed: " + e.getMessage(), e);
        }
    }

    private int parseRows(String rows) {
        if (rows == null || rows.isEmpty()) {
            return 1000;
        }
        try {
            return Integer.parseInt(rows);
        } catch (NumberFormatException e) {
            return 1000;
        }
    }

    private Collection<Concept> mapToConcepts(List<Map<String, Object>> results) {
        List<Concept> concepts = new ArrayList<>();

        for (Map<String, Object> row : results) {
            Concept concept = new Concept();

            Object conceptId = row.get("concept_id");
            if (conceptId != null) {
                concept.conceptId = ((Number) conceptId).longValue();
            }

            concept.conceptName = (String) row.get("concept_name");
            concept.domainId = (String) row.get("domain_id");
            concept.vocabularyId = (String) row.get("vocabulary_id");
            concept.conceptClassId = (String) row.get("concept_class_id");
            concept.standardConcept = (String) row.get("standard_concept");
            concept.conceptCode = (String) row.get("concept_code");
            concept.invalidReason = (String) row.get("invalid_reason");

            Object validStartDate = row.get("valid_start_date");
            if (validStartDate instanceof java.util.Date) {
                concept.validStartDate = (java.util.Date) validStartDate;
            } else if (validStartDate instanceof java.sql.Date) {
                concept.validStartDate = new java.util.Date(((java.sql.Date) validStartDate).getTime());
            }

            Object validEndDate = row.get("valid_end_date");
            if (validEndDate instanceof java.util.Date) {
                concept.validEndDate = (java.util.Date) validEndDate;
            } else if (validEndDate instanceof java.sql.Date) {
                concept.validEndDate = new java.util.Date(((java.sql.Date) validEndDate).getTime());
            }

            concepts.add(concept);
        }

        return concepts;
    }
}
