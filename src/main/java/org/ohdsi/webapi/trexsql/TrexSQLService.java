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
 * Service for TrexSQL operations used by SearchProvider.
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

    public boolean isEnabledForSource(String sourceKey) {
        return config.isEnabledForSource(sourceKey);
    }

    public boolean isCacheAvailable(String sourceKey) {
        TrexSQLSourceConfig sourceConfig = config.getSourceConfig(sourceKey);
        if (sourceConfig == null) {
            return false;
        }
        String databaseCode = sourceConfig.getDatabaseCode();
        if (databaseCode == null || databaseCode.isEmpty()) {
            return false;
        }
        return Paths.get(config.getCachePath(), databaseCode + ".db")
            .toFile().exists();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchVocab(String sourceKey, String searchTerm, int maxRows) {
        log.debug("Searching vocabulary for source {} with term: {}", sourceKey, searchTerm);

        TrexSQLSourceConfig sourceConfig = config.getSourceConfig(sourceKey);
        if (sourceConfig == null) {
            throw new IllegalStateException("TrexSQL source configuration not found for key: " + sourceKey);
        }

        String databaseCode = sourceConfig.getDatabaseCode();
        if (databaseCode == null || databaseCode.isEmpty()) {
            throw new IllegalStateException("TrexSQL database code not configured for source: " + sourceKey);
        }

        Map<String, Object> options = new HashMap<>();
        options.put("database-code", databaseCode);
        options.put("max-rows", maxRows);
        String cachePath = config.getCachePath();
        options.put("cache-path", cachePath != null ? cachePath : "/data/cache");

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
