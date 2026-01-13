package org.ohdsi.webapi.trexsql;

import org.trex.Trexsql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for TrexSQL operations. Cache is available if file exists.
 */
@Service
@ConditionalOnProperty(name = "trexsql.enabled", havingValue = "true", matchIfMissing = false)
public class TrexSQLService {

    private static final Logger log = LoggerFactory.getLogger(TrexSQLService.class);

    private final TrexSQLConfig config;
    private final TrexSQLInstanceManager instanceManager;

    public TrexSQLService(TrexSQLConfig config, TrexSQLInstanceManager instanceManager) {
        this.config = config;
        this.instanceManager = instanceManager;
    }

    /**
     * Check if cache file exists for source.
     */
    public boolean isCacheAvailable(String sourceKey) {
        return Paths.get(config.getCachePath(), sourceKey + ".db").toFile().exists();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchVocab(String sourceKey, String searchTerm, int maxRows) {
        log.debug("Searching vocabulary for source {} with term: {}", sourceKey, searchTerm);

        if (!isCacheAvailable(sourceKey)) {
            throw new IllegalStateException("TrexSQL cache not available for source: " + sourceKey);
        }

        Map<String, Object> options = new HashMap<>();
        options.put("database-code", sourceKey);
        options.put("max-rows", maxRows);
        options.put("cache-path", config.getCachePath());

        try {
            Object db = instanceManager.getInstance();
            List<Map<String, Object>> results = Trexsql.searchVocab(db, searchTerm, options);
            log.debug("Vocabulary search returned {} results", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error searching vocabulary for source {}: {}", sourceKey, e.getMessage(), e);
            throw new RuntimeException("Vocabulary search failed: " + e.getMessage(), e);
        }
    }
}
